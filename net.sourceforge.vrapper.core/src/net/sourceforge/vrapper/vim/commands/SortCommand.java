package net.sourceforge.vrapper.vim.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.vrapper.platform.SimpleConfiguration;
import net.sourceforge.vrapper.utils.NumericStringComparator;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * 7. Sorting text						*sorting*
 * Vim has a sorting function and a sorting command.  The sorting function can be
 *   *:sor* *:sort*
 *    :[range]sor[t][!] [i][u][r][n][x][o] [/{pattern}/]
 * Sort lines in [range].  When no range is given all
 * lines are sorted.
 * 
 * With [!] the order is reversed.
 * 
 * With [i] case is ignored.
 * 
 * With [n] sorting is done on the first decimal number
 * in the line (after or inside a {pattern} match).
 * One leading '-' is included in the number.
 *  
 * With [x] sorting is done on the first hexadecimal
 * number in the line (after or inside a {pattern}
 * match).  A leading "0x" or "0X" is ignored.
 * One leading '-' is included in the number. 
 *
 * With [o] sorting is done on the first octal number in
 * the line (after or inside a {pattern} match).
 *  
 * With [u] only keep the first of a sequence of
 * identical lines (ignoring case when [i] is used).
 * Without this flag, a sequence of identical lines
 * will be kept in their original order.
 * Note that leading and trailing white space may cause
 * lines to be different.
 *
 * When /{pattern}/ is specified and there is no [r] flag
 * the text matched with {pattern} is skipped, so that
 * you sort on what comes after the match.
 * Instead of the slash any non-letter can be used.
 * For example, to sort on the second comma-separated
 * field:  
 * 			:sort /[^,]*,/ 
 * To sort on the text at virtual column 10 (thus
 * ignoring the difference between tabs and spaces):  
 * 			:sort /.*\%10v/
 * To sort on the first number in the line, no matter
 * what is in front of it:  
 * 			:sort /.\{-}\ze\d/
 * (Explanation: ".\{-}" matches any text, "\ze" sets the
 * end of the match and \d matches a digit.)
 * 
 * With [r] sorting is done on the matching {pattern} 
 * instead of skipping past it as described above.
 * For example, to sort on only the first three letters
 * of each line:  
 * 			:sort /\a\a\a/ r
 *  
 * If a {pattern} is used, any lines which don't have a
 * match for {pattern} are kept in their current order,
 * but separate from the lines which do match {pattern}.
 * If you sorted in reverse, they will be in reverse
 * order after the sorted lines, otherwise they will be
 * in their original order, right before the sorted
 * lines. 
 * 
 * If {pattern} is empty (e.g. // is specified), the
 * last search pattern is used.  This allows trying out
 * a pattern first. 
 * 
 * Note that using `:sort` with `:global` doesn't sort the 
 * matching lines, it's quite useless. 
 * 
 * The details about sorting depend on the library function used.
 * There is no guarantee that sorting is "stable" or obeys the 
 * current locale. You will have to try it out.
 *  
 * The sorting can be interrupted, but if you interrupt it too late in the
 * process you may end up with duplicated lines. This also depends on the system
 * library function used. 
 *
 * @author Brian Detweiler
 *
 */
public class SortCommand extends CountIgnoringNonRepeatableCommand {
	
	private static enum Options {
		
		Options() {
			
		}
		
	    NUMERIC_SORT,
	    REVERSED_SORT,
	    IGNORE_CASE,
	    HEX,
	    OCTAL,
	    UNIQUE,
	    USE_PATTERN;
    
	}
	
    private boolean numeric = false;
    
    private boolean reversed = false;
    
    private boolean ignoreCase = false;
    
    private boolean hex = false;
    
    private boolean octal = false;
    
    private boolean unique = false;
    
    private boolean usePattern = false;
    
    public SortCommand() {
        super();
    }
    
    /**
     * sort takes an optional argument of "n" for Numeric sort.
     * @param option
     * @throws CommandExecutionException
     */
    public SortCommand(String option) throws CommandExecutionException {
        super();
        if(NUMERIC_SORT.equalsIgnoreCase(option))
        	numeric = true;
        else
        	throw new CommandExecutionException("Invalid argument: " + option);
    }
    
	public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        int line = editorAdaptor.getViewContent().getLineInformationOfOffset(
                editorAdaptor.getPosition().getViewOffset()).getNumber();
        doIt(editorAdaptor, line);
	}

    public void doIt(EditorAdaptor editorAdaptor, int line) {
  
    	SimpleConfiguration config = new SimpleConfiguration();
    	String nl = config.getNewLine();
    	
    	int length = editorAdaptor.getModelContent().getTextLength();
    	String editorContent = editorAdaptor.getModelContent().getText(0, length);
   
    	char[] editorContentArr = editorContent.toCharArray();
  
    	List<String> editorContentList = new ArrayList<String>();
    	String s = "";
    	for(char c : editorContentArr) {
    		s += c;
    		if(nl.equalsIgnoreCase(c + "")) {
    			editorContentList.add(s);
    			s = "";
    		}
    	}
    
    	// If the last line is a new line, we need to explicitly add that
    	if(nl.equalsIgnoreCase(editorContentArr[editorContentArr.length - 1] + ""))
			editorContentList.add(nl);
    	// Otherwise, we can just add the last line
    	else
			editorContentList.add(s + nl);
    	
    	if(numeric) {
    		NumericStringComparator nsc = new NumericStringComparator();
    		Collections.sort(editorContentList, nsc);
    	} else
    		Collections.sort(editorContentList);
  
    	int size = editorContentList.size();
    	int count = 0;
    	String replacementText = "";
    	for(String editorLine : editorContentList) {
    		++count;
    		if(count == size && editorLine.endsWith(nl))
    			editorLine = editorLine.substring(0, editorLine.length() - 1);
    		replacementText += editorLine;
    	}
    
		editorAdaptor.getModelContent().replace(0, length, replacementText);
    }
    
    public boolean isNumeric() {
    	return numeric;
    }
    
    public void setNumeric(boolean numeric) {
    	this.numeric = numeric;
    }

	public boolean isReversed() {
		return reversed;
	}

	public void setReversed(boolean reversed) {
		this.reversed = reversed;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public boolean isHex() {
		return hex;
	}

	public void setHex(boolean hex) {
		this.hex = hex;
	}

	public boolean isOctal() {
		return octal;
	}

	public void setOctal(boolean octal) {
		this.octal = octal;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public boolean isUsePattern() {
		return usePattern;
	}

	public void setUsePattern(boolean usePattern) {
		this.usePattern = usePattern;
	}
}