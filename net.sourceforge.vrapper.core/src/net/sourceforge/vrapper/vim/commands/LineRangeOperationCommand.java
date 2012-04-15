package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Perform an operation (yank, delete) on a range of lines.
 * Supports the following line definitions along with +/- modifiers:
 * <number> (line number), . (current line), $ (last line), '<x> (mark)
 * For example:
 * :3,4d
 * :3-2,3+6 d
 * :3,$y
 * :.+2,$-4y
 * :'a,'bd
 **/
public class LineRangeOperationCommand extends CountIgnoringNonRepeatableCommand {

	private String definition;
	
	/**
	 * @param definition User-provided string to define the range operation.
	 */
	public LineRangeOperationCommand(String definition) {
		this.definition = definition;
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
    	boolean delimFound = false;
    	
    	for(int i=0; i < command.length(); i++) {
    		char next = command.charAt(i);
    		if(next == ' ') { //ignore all spaces
    			continue;
    		}
    		
    		if(! delimFound) { //still building first part of range
    			if(next == ',') { //is this the only range delimiter?
    				delimFound = true;
    			}
    			else {
    				startStr += next;
    			}
    		}
    		else {  //building second part of range
    			if(next == 'd' || next == 'y') { //what other operations do we support?
    				operationChar = next;
    				break; //ignore anything beyond the operation
    			}
    			else {
    				stopStr += next;
    			}
    		}
    	}
    	
    	if(startStr.length() == 0 || stopStr.length() == 0 || operationChar == 0) {
    		//didn't parse right for whatever reason
    		return null;
    	}
    	
    	Position startPos = parseRangePosition(startStr, editorAdaptor);
    	Position stopPos = parseRangePosition(stopStr, editorAdaptor);
    	SimpleTextOperation operation = parseRangeOperation(operationChar, editorAdaptor);
    	
    	if(startPos != null && stopPos != null && operation != null) {
    		return new TextOperationTextObjectCommand(operation, new LineWiseSelection(editorAdaptor, startPos, stopPos));
    	}
    	else {
    		return null;
    	}
    }
    
    /**
     * parse line definition for range operations
     */
    private Position parseRangePosition(String range, EditorAdaptor editorAdaptor) {
    	CursorService cursorService = editorAdaptor.getCursorService();
    	TextContent modelContent = editorAdaptor.getModelContent();
    	
    	String lineDef;
    	String modifierDef = "";
    	char modifier = 0;
    	String[] pieces;
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
    	else {
    		lineDef = range;
    	}
    	
    	
    	Position pos;
    	if(lineDef.startsWith("'") && lineDef.length() > 1) { //mark
    		pos = cursorService.getMark(lineDef.substring(1));
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
    			return null;
    		}
    	}
    	
    	if(modifier != 0) {
    		int modifierCount;
    		try {
    			modifierCount = Integer.parseInt(modifierDef);
    		} catch (NumberFormatException e) {
    			//there wasn't a number after the +/-
    			return null;
    		}
    		
    		LineInformation posLine = modelContent.getLineInformationOfOffset( pos.getModelOffset() );
    		int lineNumber = posLine.getNumber();
    		if(modifier == '+') {
    			lineNumber += modifierCount;
    		}
    		else {
    			lineNumber -= modifierCount;
    		}
    		
    		if(lineNumber < 0 || lineNumber > modelContent.getNumberOfLines() -1) {
    			editorAdaptor.getUserInterfaceService().setErrorMessage("Invalid Range");
    			return null;
    		}
    		
    		LineInformation newLine = modelContent.getLineInformation(lineNumber);
    		pos = cursorService.newPositionForModelOffset( newLine.getBeginOffset() );
    	}
    	
    	return pos;
    }
    
    /**
     * parse operation for range operations
     */
    private SimpleTextOperation parseRangeOperation(char operation, EditorAdaptor editorAdaptor) {
    	if(operation == 'y') {
    		return YankOperation.INSTANCE;
    	}
    	else if(operation == 'd') {
    		return DeleteOperation.INSTANCE;
    	}
    	else {
    		editorAdaptor.getUserInterfaceService().setErrorMessage("Unknown operation for range: " + operation);
    		return null;
    	}
    }
}
