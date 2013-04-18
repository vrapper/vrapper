package net.sourceforge.vrapper.vim.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.RangeSearchMotion;

/**
 * Perform an operation (yank, delete, substitution) on a range of lines.
 * Supports the following line definitions along with +/- modifiers:
 * <number> (line number), . (current line), $ (last line),
 * '<x> (mark), /something/ (search forward), ?something? (search backward)
 * 
 * For example:
 * :3,4d
 * :3-2,3+6 d
 * :3,$y
 * :.+2,$-4y
 * :'a,'bd
 * :2,/foo/y
 * 
 * Since there is a lot of string parsing going on, it is very procedural.
 * I've broken the process into methods to make it easier to follow.
 **/
public class LineRangeOperationCommand extends CountIgnoringNonRepeatableCommand {

	private String definition;
	
	/**
	 * @param definition User-provided string to define the range operation.
	 */
	public LineRangeOperationCommand(String definition) {
		this.definition = definition;
	}
	
	public static boolean isLineRangeOperation(String command) {
		//list all possible starting characters for a line range
		//<number> $ / ? . ' + - , 
		return command.matches("^[\\d\\$\\/\\?\\.\\'\\+\\-\\,].*");
	}

	public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
		TextOperationTextObjectCommand command = parseRangeDefinition(definition, editorAdaptor);
		
		if(command != null) {
			command.execute(editorAdaptor);
		}
	}

    private TextOperationTextObjectCommand parseRangeDefinition(String command, EditorAdaptor editorAdaptor) {
    	String startStr    = "";
    	String stopStr     = "";
    	char operationChar = 0;
    	String remainingChars = "";
    	boolean delimFound = false;
    	boolean insideSearchDef = false;
    	
    	for(int i=0; i < command.length(); i++) {
    		char next = command.charAt(i);
    		//ignore all spaces (unless we're defining search criteria)
    		if(next == ' ' && !insideSearchDef) {
    			continue;
    		}
    		
    		//if we're defining a search, don't flag any characters
    		//as being special (no delimiters, no operations)
    		if(next == '/' || next == '?') {
    			//the '/' or '?' character has to start and end the search definition
    			//so we can just toggle whatever the previous value was
    			insideSearchDef = ! insideSearchDef;
    		}
    		
    		if(! delimFound) { //still building first part of range
    			if(!insideSearchDef && next == ',') { //is this the only range delimiter?
    				delimFound = true;
    			}
    			else {
    				startStr += next;
    			}
    		}
    		else { //building second part of range
    			if(!insideSearchDef && isOperationChar(next) && ! stopStr.endsWith("'")) {
    				operationChar = next;
    				//if 's', we're defining a substitution for the range
    				remainingChars = command.substring(i);
    				break; //we've found everything we need
    			}
    			else {
    				stopStr += next;
    			}
    		}
    	}
    	
    	//if range not defined, assume current position
    	if(startStr.length() == 0 || startStr.startsWith("+") || startStr.startsWith("-") ) {
    		startStr = "." + startStr;
    	}
    	if(stopStr.length() == 0 || stopStr.startsWith("+") || stopStr.startsWith("-") ) {
    		stopStr = "." + stopStr;
    	}
    	if(operationChar == 0) {
    		//didn't parse right for whatever reason
    		return null;
    	}
    	
    	Position startPos = parseRangePosition(startStr, editorAdaptor);
    	Position stopPos = parseRangePosition(stopStr, editorAdaptor);
    	
    	SimpleTextOperation operation = parseRangeOperation(operationChar, remainingChars, editorAdaptor);
    	
    	if(startPos != null && stopPos != null && operation != null) {
    		return new TextOperationTextObjectCommand(operation, new LineWiseSelection(editorAdaptor, startPos, stopPos));
    	}
    	else {
    		return null;
    	}
    }
   
    /*
     * When parsing a range, this determines the first letter of the actual operation we're
     * trying to do, so stop parsing the range. Range operations include :sort, :retab, :yank, :delete
     */
    private boolean isOperationChar(char c) {
    	//what other operations do we support?
    	return c == 'd' || c == 'y' || c == 's' || c == 'v' || c == 'g' || c == 'r';
    }
    
    /**
     * Parse user-defined line definition with optional +/- modifiers.
     * In the definition <range>,<range><operation> this is one of the
     * two <range>s.  This method doesn't care if its the start or end
     * we're defining.
     * @param range - user-defined string to define a line in the file
     * @return Position in the file corresponding to range
     */
    private Position parseRangePosition(String range, EditorAdaptor editorAdaptor) {
    	String lineDef;
    	String modifierDef = "";
    	char modifier = 0;
    	String[] pieces;
    	Pattern endsWithNumber = Pattern.compile("^\\D+?(\\d+)$");
    	Matcher match = endsWithNumber.matcher(range);
    	
    	//are there modifiers like 3+2 or .-4 or 'a+6?
    	if(range.contains("+")) {
    		modifier = '+';
    		pieces = range.split("\\+");
    		lineDef = pieces[0];
    		modifierDef = pieces[1];
    	}
    	else if(range.contains("-")) {
    		modifier = '-';
    		pieces = range.split("-");
    		lineDef = pieces[0];
    		modifierDef = pieces[1];
    	}
    	else if(match.matches()) {
    		//if no '+' or '-' specified, '+' is implicit
    		// ".5" == ".+5"
    		modifier = '+';
    		lineDef = range.substring(0, match.start(1));
    		modifierDef = match.group(1);
    	}
    	else { //no modifiers
    		lineDef = range;
    	}
    	
    	Position pos = parseLineDefinition(lineDef, editorAdaptor);
    	if(pos == null) {
    		//couldn't find a position
    		return null;
    	}
    	
    	if(modifier != 0) {
    		//take the current position and increment or decrement it
    		pos = parseModifierDefinition(modifier, modifierDef, pos.getModelOffset(), editorAdaptor);
    	}
    	
    	return pos;
    }
    
    /**
     * Parse the line definition (minus any modifiers) and return
     * the corresponding Position.
     * @param lineDef - user-provided string to define a position
     * @param editorAdaptor
     * @return Position in the file matching lineDef
     */
    private Position parseLineDefinition(String lineDef, EditorAdaptor editorAdaptor) {
    	CursorService cursorService = editorAdaptor.getCursorService();
    	TextContent modelContent = editorAdaptor.getModelContent();
    	
    	Position pos = null;
    	if(lineDef.startsWith("'") && lineDef.length() > 1) { //mark
    		String mark = lineDef.substring(1);
    		if(mark.equals("<")) { //selection begin
    			try {
					pos = editorAdaptor.getSelection().getRegion(editorAdaptor, 0).getLeftBound();
				} catch (CommandExecutionException e) { }
    		}
    		else if(mark.equals(">")) { //selection end
    			try {
    				//getRightBound is exclusive, meaning in linewise-mode it will
    				//include the first character on the next line.  Back up one
    				//character to make this inclusive.
    				pos = editorAdaptor.getSelection().getRegion(editorAdaptor, 0).getRightBound().addModelOffset(-1);
				} catch (CommandExecutionException e) { }
    		}
    		else { //normal mark
    			pos = cursorService.getMark(mark);
    		}
    	}
    	else if(lineDef.startsWith("/") || lineDef.startsWith("?")) {
    		pos = parseSearchPosition(lineDef, editorAdaptor);
    	}
    	else if(".".equals(lineDef)) { //current line
    		pos = cursorService.getPosition();
    	}
    	else if("$".equals(lineDef)) { //last line
    		pos = cursorService.newPositionForModelOffset( modelContent.getTextLength()-1 );
    	}
    	else { //maybe a line number?
    		try {
    			int line = Integer.parseInt(lineDef);
    			if(line > 0) {
    				line--; //0-based indexing internally
    			}
    			if(line > modelContent.getNumberOfLines() -1) {
    				editorAdaptor.getUserInterfaceService().setErrorMessage("Invalid Range");
    				return null;
    			}
    			pos = cursorService.newPositionForModelOffset( modelContent.getLineInformation(line).getBeginOffset() );
    		} catch (NumberFormatException e) {
    			//it wasn't a number
    		}
    	}
    	
    	return pos;
    }
    
    /**
     * Search for the provided string and return its Position.
     * Start searching from the current cursor position.
     * @param searchDef - user-defined search, /something/ or ?something?
     * @param editorAdaptor
     * @return Position of the match closest to the cursor
     */
    private Position parseSearchPosition(String searchDef, EditorAdaptor editorAdaptor) {
    	Position start = editorAdaptor.getPosition();
    	boolean reverse = searchDef.startsWith("?");
    	
    	//chop off the leading and trailing '/' or '?'
    	String search = searchDef.substring(1, searchDef.length()-1);
    	try {
			Position pos = new RangeSearchMotion(search, start, reverse).destination(editorAdaptor);
			if(pos == null) {
				editorAdaptor.getUserInterfaceService().setErrorMessage("'"+search+"' not found");
			}
			return pos;
		} catch (CommandExecutionException e) {
			return null;
		}
    }
    
    /**
     * Parse the modifiers defined after the lineDef.
     * This takes an incoming startOffset and increments
     * or decrements it.
     * @param modifier - a '+' or '-' to increment or decrement by modifierDef
     * @param modifierDef - number of lines away from startOffset desired
     * @param startOffset - beginning position (if no modifiers had been defined)
     * @param editorAdaptor
     * @return new Position based off startOffset
     */
    private Position parseModifierDefinition(char modifier, String modifierDef, int startOffset,
    		EditorAdaptor editorAdaptor ) {
    	CursorService cursorService = editorAdaptor.getCursorService();
    	TextContent modelContent = editorAdaptor.getModelContent();
    	
    	int modifierCount;
    	try {
    		modifierCount = Integer.parseInt(modifierDef);
    	} catch (NumberFormatException e) {
    		//there wasn't a number after the +/-
    		return null;
    	}

    	LineInformation startLine = modelContent.getLineInformationOfOffset(startOffset);
    	int lineNumber = startLine.getNumber();
    	if(modifier == '+') {
    		lineNumber += modifierCount;
    	}
    	else { //modifier == '-'
    		lineNumber -= modifierCount;
    	}

    	if(lineNumber < 0 || lineNumber > modelContent.getNumberOfLines() -1) {
    		editorAdaptor.getUserInterfaceService().setErrorMessage("Invalid Range");
    		return null;
    	}

    	LineInformation newLine = modelContent.getLineInformation(lineNumber);
    	return cursorService.newPositionForModelOffset( newLine.getBeginOffset() );
    }
    
    /**
     * Parse the desired operation to perform on this range.
     * @param operation - single character defining the operation
     * @param remainingChars - any characters defined by the user after the operation char
     * @return the Operation corresponding to the operation char
     */
    private SimpleTextOperation parseRangeOperation(char operation, String remainingChars, EditorAdaptor editorAdaptor) {
    	if(operation == 'y') {
    		return YankOperation.INSTANCE;
    	}
    	else if(operation == 'd') {
    		return DeleteOperation.INSTANCE;
    	}
    	else if(operation == 's' && remainingChars.startsWith("sort")) {
    		return new SortOperation(remainingChars.substring(4));
    	}
    	else if(operation == 's') {
    		return new SubstitutionOperation(remainingChars);
    	}
    	else if(operation == 'g' || operation == 'v') {
    		return new ExCommandOperation(remainingChars);
    	}
    	else if(operation == 'r' && (remainingChars.startsWith("ret"))) {
    	    return new RetabOperation(remainingChars);
    	}
    	else {
    		editorAdaptor.getUserInterfaceService().setErrorMessage("Unknown operation for range: " + operation);
    		return null;
    	}
    }
}
