package net.sourceforge.vrapper.vim.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.LineRange;
import net.sourceforge.vrapper.utils.NumericStringComparator;
import net.sourceforge.vrapper.utils.PatternSortComparator;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.SimpleLineRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

/**
 * From the Vim manual:
 * <pre>
 * 7. Sorting text						*sorting*
 * Vim has a sorting function and a sorting command.  The sorting function can be
 *   *:sor* *:sort*
 *    :[range]sor[t][!] [i][u][r][n][b][x][o] [/{pattern}/]
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
 * XXX: NOT ORIGINALLY PART OF VIM
 * With [b] sorting is done on the first binary
 * number in the line (after or inside a {pattern}
 * match). 
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
 * </pre>
 * 
 * TODO: Pattern has not yet fully been implemented.
 * 		 This has been giving me problems, mostly because
 * 		 of the differences between Java regular expressions
 * and, well, every other regular expression implementation.
 * 
 * 		 Many of the simple examples in the Vim docs won't work
 * 		 in a Java regex. The decision has to be made, do we make
 * 		 the user use Java regex's or do we translate? Translation
 * 		 would be a huge pain, but it would give the user a 
 * familiarity benefit.
 * 
 * <pre>
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
 * </pre> 
 * 
 * @author Brian Detweiler
 * 
 */
public class SortOperation extends AbstractLinewiseOperation {

    private static final String REVERSED_FLAG    = "!";
    private static final String NUMERIC_FLAG     = "n";
    private static final String IGNORE_CASE_FLAG = "i";
    private static final String BINARY_FLAG      = "b";
    private static final String OCTAL_FLAG       = "o";
    private static final String HEX_FLAG         = "x";
    private static final String UNIQUE_FLAG      = "u";
    private static final String USE_PATTERN_R    = "r";

    // Possible configurations for sort
    /** ! - reversed sort (entered as a modifier to :sort, as :sort! */
    private boolean reversed = false;
    /** n - numeric sort */
    private boolean numeric = false;
    /** i - ignore case */
    private boolean ignoreCase = false;
    /** b - binary sort */
    private boolean binary = false;
    /** x - hexadecimal sort */
    private boolean hex = false;
    /** o - octal sort */
    private boolean octal = false;
	/** u - unique - removes duplicate entries */
    private boolean unique = false;
    /** /regex pattern/ */
    private boolean usePattern = false;
    /** /regex pattern/ r */
    private boolean usePatternR = false;

    private String pattern = null;

    public SortOperation(String commandStr) {
        super();
        
        pattern = parsePattern(commandStr);
        if(pattern != null) {
        	//remove pattern so we don't attempt to parse options from it
        	commandStr = commandStr.replace(pattern, "");
            usePattern = true;
            //remove pattern delimiters (e.g., '/')
            pattern = pattern.substring(1, pattern.length() -1);
        }

        String[] options = commandStr.split("");
        for (String option : options) {
            if (option == null || option.trim().isEmpty())
                continue;
            else if (option.equalsIgnoreCase(REVERSED_FLAG))
                reversed = true;
            else if (option.equalsIgnoreCase(NUMERIC_FLAG))
                numeric = true;
            else if (option.equalsIgnoreCase(IGNORE_CASE_FLAG))
                ignoreCase = true;
            else if (option.equalsIgnoreCase(BINARY_FLAG))
                binary = true;
            else if (option.equalsIgnoreCase(OCTAL_FLAG))
                octal = true;
            else if (option.equalsIgnoreCase(HEX_FLAG))
                hex = true;
            else if (option.equalsIgnoreCase(UNIQUE_FLAG))
                unique = true;
            else if (usePattern && option.equalsIgnoreCase(USE_PATTERN_R))
                usePatternR = true;
        }
    }
    
    /**
     * Patterns can have any non-word character as a delimiter.
     * For example, "/pattern/", "_pattern_", or ":pattern:".
     * Find the first non-word character in the string and see
     * if it has a match.  If it does, return that string.
     * @param command - string of :sort options
     * @return - string pattern (with delimiters), or null if none found
     */
    private String parsePattern(String command) {
        char[] chars = command.toCharArray();
        String c;
        for (int i=0; i < chars.length; i++) {
        	c = chars[i] + "";
        	if(c.trim().isEmpty()) {
        		//skip (but don't remove) whitespace
        		continue;
        	}
            if (VimUtils.isPatternDelimiter(c)) {
            	//this could be a pattern delimiter
            	int match = command.indexOf(c.charAt(0), i+1);
            	if(match == -1) {
            		//we found a non-word character with no match
            		//probably an invalid character
            		return null;
            	}
            	else {
            		return command.substring(i, match+1);
            	}
            }
        }
        return null;
    }

    /**
     * According to Vim behavior, sorting by number will look at the FIRST
     * OCCURRENCE of contiguous number string on a line.
     * The following return true:
     *      1
     *      9L
     *      67 Chevy
     *      -29
     *      blah blah 5 blah blah
     *      0b01010
     *      01234567123
     *      Ox123
     * NOTE: These will match to the very FIRST occurrence of a number in their
     * set. If it finds one, then we return true.
     * 
     * @param str
     * @param base
     * @return
     */
    private boolean hasNumber(String str, int offset) {
    	int radix = 10;
    	     if(binary) radix = 2;
    	else if(octal)  radix = 8;
    	else if(hex)    radix = 16;
    	
    	if(offset > 0) {
    		str = str.substring(offset);
    	}
        char[] strArr = str.toCharArray();
        for (char c : strArr) {
            if (Character.digit(c, radix) != -1)
                return true;
        }

        return false;
    }

	@Override
    public void execute(EditorAdaptor editorAdaptor, LineRange lineRange) throws CommandExecutionException {
        try {
        	TextContent content = editorAdaptor.getModelContent();
        	LineInformation startLine;
        	LineInformation endLine;
        	int length;
        	
        	startLine = content.getLineInformation(lineRange.getStartLine());
        	endLine = content.getLineInformation(lineRange.getEndLine());
        	length = lineRange.getModelLength();
        	
        	//don't sort if only one line
        	//(or if start and end are somehow swapped)
        	if(startLine.getNumber() < endLine.getNumber())
	            doIt(editorAdaptor, startLine, endLine, length);
        	
        } catch (Exception e) {
            throw new CommandExecutionException("sort failed: " + e.getMessage());
        }
    }

    @Override
    public LineRange getDefaultRange(EditorAdaptor editorAdaptor, int count, Position currentPos)
            throws CommandExecutionException {
        return SimpleLineRange.entireFile(editorAdaptor);
    }

    /**
     * This is where the action happens.
     * 
     * @param editorAdaptor
     * @throws Exception
     */
    public void doIt(EditorAdaptor editorAdaptor, LineInformation startLine,
    		LineInformation endLine, int totalLengthOfRange) throws Exception {
        // Throw the whole editor into an array separated by newlines
        String newline = editorAdaptor.getConfiguration().getNewLine();
        TextContent content = editorAdaptor.getModelContent();
        int totalLinesInEditor = content.getNumberOfLines();
        List<String> editorContentList = new ArrayList<String>();
        Comparator<String> comp = null;
        LineInformation line = null;
       
        /* 
         * Step 1: Put editor text into a sortable array
         *         This may be the whole editor or a range
         */
        for(int i = startLine.getNumber(); i <= endLine.getNumber(); ++i) {
            line = content.getLineInformation(i);
            String lineStr = content.getText(line.getBeginOffset(), line.getLength());
            editorContentList.add(lineStr);
        }
       
        /*
         * Step 2: If u was specified, get all unique lines 
         *         and remove the rest
         */
        if (unique) {
        	//use a Set to remove all duplicates for us
        	Set<String> set;
        	if(ignoreCase) {
        		//TreeSet can take a Comparator as an argument to constructor
        		set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        	} else {
        		set = new TreeSet<String>();
        	}
        	set.addAll(editorContentList);
            editorContentList = new ArrayList<String>(set);
            totalLinesInEditor = editorContentList.size();
        }
       
        /*
         * Step 3: If a pattern was used, handle it
         */
        List<String> candidateList = editorContentList;
        List<Integer> candidateOffsetList = new ArrayList<Integer>();
        List<String> nonCandidateList = new ArrayList<String>();
        if(usePattern) {
            String candidate;
            for (int i = 0; i < candidateList.size(); i++) {
            	candidate = candidateList.get(i);

            	if(candidate.contains(pattern)) {
            		candidateOffsetList.add( usePatternR ? candidate.indexOf(pattern) : candidate.indexOf(pattern) + pattern.length() );
            	}
            	else {
                	candidateList.remove(i);
                	i--;
            		nonCandidateList.add(candidate);
            	}
            }
            
            comp = new PatternSortComparator(pattern, usePatternR);
        }

        /*
         * Step 4: If a numeric sort was called, separate numeric vs. non-numeric
         *         Also handle ignore case
         */
        if (numeric || binary || octal || hex) {
        	comp = new NumericStringComparator(binary, octal, hex, pattern, usePatternR);

        	String candidate;
        	int candidateOffset;
            for (int i=0; i < candidateList.size(); i++) {
            	candidate = candidateList.get(i);
            	candidateOffset = candidateOffsetList.size() > i ? candidateOffsetList.get(i) : -1;
                if (! hasNumber(candidate, candidateOffset)) {
                	candidateList.remove(candidate);
                	i--;
                    nonCandidateList.add(candidate);
                }
            }
        } else if(ignoreCase) {
        	comp = String.CASE_INSENSITIVE_ORDER;
        }
        
        /*
         * Step 5: Perform the actual sorting on all sortable candidates
         */
        if(comp == null) { // normal ascii sort
            Collections.sort(candidateList);
        } else {
            Collections.sort(candidateList, comp);
        }
        
        // Add non-sorted rows before sorted rows, per Vim behavior
        editorContentList = new ArrayList<String>(nonCandidateList);
        editorContentList.addAll(candidateList);

        if (reversed) {
            Collections.reverse(editorContentList);
        }

        /*
         * Step 6: Append newlines to everything but the very last line of the editor
         */
        StringBuilder replacementText = new StringBuilder();
        int count = startLine.getNumber();
        for (String editorLine : editorContentList) {
            if(count != totalLinesInEditor - 1) {
                editorLine += newline;
            }
            ++count;
            replacementText.append(editorLine);
        }
        
        /*
         * Step 7: Replace the contents of the editor with the freshly sorted text
         *         This applies to a range, or the whole editor
         */
        editorAdaptor.getModelContent().replace(
        		startLine.getBeginOffset(),
        		totalLengthOfRange,
        		replacementText.toString()
        		
		);
        //put cursor at beginning of sorted text
        editorAdaptor.setPosition(
        		editorAdaptor.getCursorService().newPositionForModelOffset(startLine.getBeginOffset()),
        		StickyColumnPolicy.ON_CHANGE
        );
    }

	public TextOperation repetition() {
		return null;
	}
}