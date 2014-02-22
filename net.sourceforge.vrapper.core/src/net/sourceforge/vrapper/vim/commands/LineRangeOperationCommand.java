package net.sourceforge.vrapper.vim.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.utils.LineAddressParser;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
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
	//                                               1                                      2       3                  4
	private static final String START_SEL_RE = "^\\s*(%|\\$|[+-.]?\\d+|\\.|\\^|'[<\\[a-zA-Z]|([?\\/])((?:\\\\2|.)+?)\\2)([+-]\\d+)?\\s*";
	//                                                      5                                6       7                  8
	private static final String END_SEL_RE =   "\\s*[,;]\\s*([+-.]?\\d+|\\.|\\$|'[>\\]a-zA-Z]|([?\\/])((?:\\\\6|.)+?)\\6)([+-]\\d+)?\\s*";
	private static final Pattern START_AND_STOP = Pattern.compile(START_SEL_RE + END_SEL_RE + "(\\D.*)");
	private static final Pattern JUST_START = Pattern.compile(START_SEL_RE + "(\\D.*)");
	private static final Pattern JUST_STOP = Pattern.compile("^" + END_SEL_RE + "(\\D.*)");
	private static final Pattern COPY_MOVE = Pattern.compile("^(t|co(p(y)?)?|m(o(v(e?)?)?)?)\\s+.*");
	private String definition;
    private String startStr = "";
    private String stopStr = "";
	private String operationStr = "";
	
    /**
	 * @param definition User-provided string to define the range operation.
	 */
	public LineRangeOperationCommand(String definition) {
		this.definition = definition;
		parseRangeDefinition();
	}
	
	public static boolean isLineRangeOperation(String command) {
		//list all possible starting characters for a line range
		//<number> $ / ? . ' + - , 
	    return JUST_START.matcher(command).matches() || JUST_STOP.matcher(command).matches();
	}
	
	public static boolean isCurrentLineCopyMove(String command) {
	    return COPY_MOVE.matcher(command).matches();
	}
	
	public String getOperationStr() {
        return operationStr;
    }
	
	public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
	    TextObject range = parseRangeDefinition(editorAdaptor, true);
	    if (range != null)
	    {
	        TextOperation operation = parseRangeOperation(editorAdaptor);
	        if (operation != null)
	        {
	            new TextOperationTextObjectCommand(operation, range).execute(editorAdaptor);
	        }
	    }
	}

    private void parseRangeDefinition() {
        Matcher matchingRe;
        matchingRe = START_AND_STOP.matcher(definition);
        if (matchingRe.matches())
        {
            // Both positions specified
            startStr     = matchingRe.group(1) + (matchingRe.group(4) != null ? matchingRe.group(4) : "");
            stopStr      = matchingRe.group(5) + (matchingRe.group(8) != null ? matchingRe.group(8) : "");
            operationStr = matchingRe.group(9);
        } else {
            matchingRe = JUST_START.matcher(definition);
            if (matchingRe.matches()) {
                // Only start position specified
                startStr = matchingRe.group(1) + (matchingRe.group(4) != null ? matchingRe.group(4) : "");
                // One line only addressed
                stopStr = startStr;
            } else {
                // Only stop position specified
                matchingRe = JUST_STOP.matcher(definition);
                if (matchingRe.matches())
                {
                    stopStr = matchingRe.group(1) + (matchingRe.group(4) != null ? matchingRe.group(4) : "");
                }
            }
            operationStr = matchingRe.group(5);
        }
        // % is a shortcut for 0,$
        if (startStr.equals("%")) {
            startStr = "0";
            stopStr  = "$";
        } else {
            //if range not defined, assume current position
            if(startStr.length() == 0 || startStr.startsWith("+") || startStr.startsWith("-") ) {
                startStr = "." + startStr;
            }
            if(stopStr.length() == 0 || stopStr.startsWith("+") || stopStr.startsWith("-") ) {
                stopStr = "." + stopStr;
            }
        }
    }
    	
    public Selection parseRangeDefinition(EditorAdaptor editorAdaptor, boolean linewise) {
    	Position startPos = LineAddressParser.parseAddressPosition(startStr, editorAdaptor);
    	Position stopPos = LineAddressParser.parseAddressPosition(stopStr, editorAdaptor);
    	if(startPos != null && stopPos != null) {
    	    if (linewise) {
    	        return new LineWiseSelection(editorAdaptor, startPos, stopPos);
    	    } else {
    	        return new SimpleSelection(new StartEndTextRange(startPos, stopPos));
    	    }
    	}
    	else {
    		return null;
    	}
    }
    
    /**
     * Parse the desired operation to perform on this range.
     * @param operation - single character defining the operation
     * @param remainingChars - any characters defined by the user after the operation char
     * @return the Operation corresponding to the operation char
     */
    public SimpleTextOperation parseRangeOperation(EditorAdaptor editorAdaptor) {
        if (operationStr.isEmpty()) {
    		editorAdaptor.getUserInterfaceService().setErrorMessage("No operation specified.");
    		return null;
        }
        while(operationStr.startsWith(":")) {
            //remove any superfluous ':' preceding the operation
            operationStr = operationStr.substring(1);
        }

        char operation = operationStr.charAt(0);
    	if(operation == 'y') {
    	    //":y[ank] [x]" where [x] is a register
    	    if(operationStr.length() > 2 && operationStr.indexOf(' ') == operationStr.length() -2) {
    	        return new YankOperation(operationStr.substring(operationStr.length()-1), false);
    	    }
    	    else {
    	        return new YankOperation(null, false);
    	    }
    	}
    	else if(operation == 'd') {
    		return DeleteOperation.INSTANCE;
    	}
    	else if(operation == 's' && operationStr.startsWith("sort")) {
    		return new SortOperation(operationStr.substring(4));
    	}
    	else if(operation == 's') {
    		return new SubstitutionOperation(operationStr);
    	}
    	else if(operation == 'g' || operation == 'v') {
    		return new ExCommandOperation(operationStr);
    	}
    	else if(operation == 'r' && (operationStr.startsWith("ret"))) {
    	    return new RetabOperation(operationStr);
    	}
    	else if(operation == 'c' || operation == 't') {
    		return new CopyMoveLinesOperation(operationStr, false);
    	}
    	else if(operation == 'm') {
    		return new CopyMoveLinesOperation(operationStr, true);
    	}
    	else if(operation == '!') {
    		return new PipeExternalOperation(operationStr);
    	}
    	else if(ReadExternalOperation.isValid(editorAdaptor, operationStr)) {
    		return new ReadExternalOperation(operationStr);
    	}
    	else {
    		editorAdaptor.getUserInterfaceService().setErrorMessage("Unknown operation for range: " + operationStr);
    		return null;
    	}
    }
}
