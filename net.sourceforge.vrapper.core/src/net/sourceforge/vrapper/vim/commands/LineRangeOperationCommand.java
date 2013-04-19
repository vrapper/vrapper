package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.LineAddressParser;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

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
    	
    	Position startPos = LineAddressParser.parseAddressPosition(startStr, editorAdaptor);
    	Position stopPos = LineAddressParser.parseAddressPosition(stopStr, editorAdaptor);
    	
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
    	return c == 'd' || c == 'y' || c == 's' || c == 'v' || c == 'g'
    			|| c == 'r' || c == 't' || c == 'c' || c == 'm' ;
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
    	else if(operation == 'c' || operation == 't') {
    		return new CopyMoveLinesOperation(remainingChars, false);
    	}
    	else if(operation == 'm') {
    		return new CopyMoveLinesOperation(remainingChars, true);
    	}
    	else {
    		editorAdaptor.getUserInterfaceService().setErrorMessage("Unknown operation for range: " + operation);
    		return null;
    	}
    }
}
