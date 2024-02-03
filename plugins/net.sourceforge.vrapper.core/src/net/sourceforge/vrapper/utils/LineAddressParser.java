package net.sourceforge.vrapper.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.motions.RangeSearchMotion;

/**
 * :help address
 * 
 * Line numbers may be specified with:		*:range* *E14* *{address}*
 * 	{number}	an absolute line number
 * 	.		the current line			  *:.*
 * 	$		the last line in the file		  *:$*
 * 	%		equal to 1,$ (the entire file)		  *:%*
 * 	't		position of mark t (lowercase)		  *:'*
 * 	'T		position of mark T (uppercase); when the mark is in
 * 			another file it cannot be used in a range
 * 	/{pattern}[/]	the next line where {pattern} matches	  *:/*
 * 	?{pattern}[?]	the previous line where {pattern} matches *:?*
 * 	\/		the next line where the previously used search
 * 			pattern matches
 * 	\?		the previous line where the previously used search
 * 			pattern matches
 * 	\&		the next line where the previously used substitute
 * 			pattern matches
 * 
 * Each may be followed (several times) by '+' or '-' and an optional number.
 * This number is added or subtracted from the preceding line number.  If the
 * number is omitted, 1 is used.
 */
public class LineAddressParser {

    /**
     * Parse user-defined line definition with optional +/- modifiers.
     * In the definition <range>,<range><operation> this is one of the
     * two <range>s.  This method doesn't care if its the start or end
     * we're defining.
     * @param range - user-defined string to define a line in the file
     * @return Position in the file corresponding to range
     */
    public static Position parseAddressPosition(String range, EditorAdaptor editorAdaptor) {
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
    private static Position parseLineDefinition(String lineDef, EditorAdaptor editorAdaptor) {
    	CursorService cursorService = editorAdaptor.getCursorService();
    	TextContent modelContent = editorAdaptor.getModelContent();
    	
    	Position pos = null;
    	if(lineDef.startsWith("'") && lineDef.length() > 1) { //mark
    		String mark = lineDef.substring(1);
    		if(mark.equals(CursorService.LAST_SELECTION_START_MARK)) {
    			try {
    			    Selection selection = editorAdaptor.getSelection();
    			    if(selection.getModelLength() == 0) {
    			        //get last selection
    			        pos = editorAdaptor.getCursorService().getMark(CursorService.LAST_SELECTION_START_MARK);
    			    }
    			    else {
    			        //get current selection
    			        pos = editorAdaptor.getSelection().getRegion(editorAdaptor, 0).getLeftBound();
    			    }
				} catch (CommandExecutionException e) { }
    		}
    		else if(mark.equals(CursorService.LAST_SELECTION_END_MARK)) {
    			try {
    			    Selection sel = editorAdaptor.getSelection();
    			    if(sel.getModelLength() == 0) {
    			        //get last selection
    			        pos = editorAdaptor.getCursorService().getMark(CursorService.LAST_SELECTION_END_MARK);
    			    }
    			    else {
    			        //get current selection
    			        pos = editorAdaptor.getSelection().getRegion(editorAdaptor, 0).getRightBound();
    			        if (sel instanceof LineWiseSelection) {
    			            //getRightBound is exclusive, meaning in linewise-mode it will
    			            //include the first character on the next line.  Back up one
    			            //character to make this inclusive.
    			            pos = pos.addModelOffset(-1);
    			        }
    			    }
				} catch (CommandExecutionException e) { }
    		}
    		else { //normal mark
    			pos = cursorService.getMark(mark);
    		}
    	}
    	else if(lineDef.startsWith("/") || lineDef.startsWith("?")) {
    		pos = parseSearchPosition(lineDef, editorAdaptor);
    	}
    	else if(".".equals(lineDef) || lineDef.isEmpty()) { //current line
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
    private static Position parseSearchPosition(String searchDef, EditorAdaptor editorAdaptor) {
    	Position start = editorAdaptor.getPosition();
    	boolean reverse = searchDef.startsWith("?");
    	
    	//chop off the leading and trailing '/' or '?'
    	String search = searchDef.substring(1, searchDef.length()-1);
    	try {
			Position pos = new RangeSearchMotion(search, start, reverse).destination(editorAdaptor, start);
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
    private static Position parseModifierDefinition(char modifier, String modifierDef, int startOffset,
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
}
