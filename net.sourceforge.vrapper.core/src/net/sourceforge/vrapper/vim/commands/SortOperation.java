package net.sourceforge.vrapper.vim.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.IgnoreCaseStringComparator;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.NumericStringComparator;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
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
 *      With [b] sorting is done on the first binary
 * 		number in the line (after or inside a {pattern}
 * 		match). 
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
 * @author Brian Detweiler
 * 
 */
public class SortOperation extends SimpleTextOperation {

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
	/** u - works like sort -u on the command line - removes duplicate entries and sorts */
    private boolean unique = false;
    /** /regex pattern/ */
    private boolean usePattern = false;
    /** /regex pattern/ r */
    private boolean usePatternR = false;

    private String pattern = null;

    public SortOperation(String commandStr) {
        super();
        
        Pattern p = Pattern.compile("/(.*?)/(.*)");
        Matcher m = p.matcher(commandStr);

        String pattern = null;
        if(m.matches()) {
        	pattern = m.group(1);
        	commandStr = m.group(2);
        }
        
        if (pattern != null && !pattern.trim().isEmpty()) {
            this.pattern = pattern;
            usePattern = true;
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

        /* I should mention, adding "i" to a numeric sort of any type will do nothing.
         * But Vim doesn't throw an error, so we won't either. 
         * Of note, Vim does not do a secondary sort. That is, if you were to sort 
         * numerically on the following:
         *   1b
         *   2c
         *   1a
         * it would be sorted in Vim as follows:
         *   1b
         *   1a
         *   2c
         * Would it be useful to have the secondary ASCII sort, or would this break 
         * expected functionality? Leaving this up for debate. -- BRD
         */
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
    private boolean hasNumber(String str) {
    	int radix = 10;
    	     if(binary) radix = 2;
    	else if(octal)  radix = 8;
    	else if(hex)    radix = 16;
    	
        char[] strArr = str.toCharArray();
        for (char c : strArr) {
            if (Character.digit(c, radix) != -1)
                return true;
        }

        return false;
    }


	@Override
	public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType) throws CommandExecutionException {
        try {
        	TextContent content = editorAdaptor.getModelContent();
        	LineInformation startLine;
        	LineInformation endLine;
        	
        	if(region == null) {
        		startLine = content.getLineInformation(0);
        		endLine = content.getLineInformation(content.getNumberOfLines());
        	}
        	else {
        		startLine = content.getLineInformationOfOffset(region.getLeftBound().getModelOffset());
        		endLine = content.getLineInformationOfOffset(region.getRightBound().getModelOffset());
        	}
        	
        	//don't sort if only one line
        	//(or if start and end are somehow swapped)
        	if(startLine.getNumber() < endLine.getNumber())
	            doIt(editorAdaptor, startLine, endLine);
        	
        } catch (Exception e) {
            throw new CommandExecutionException("sort failed: " + e.getMessage());
        }
    }

    /**
     * This is where the action happens.
     * 
     * @param editorAdaptor
     * @throws Exception
     */
    public void doIt(EditorAdaptor editorAdaptor, LineInformation startLine, LineInformation endLine) throws Exception {
        // Throw the whole editor into an array separated by newlines
        TextContent content = editorAdaptor.getModelContent();
        List<String> editorContentList = new ArrayList<String>();
        LineInformation line = null;
        for(int i = startLine.getNumber(); i < endLine.getNumber(); ++i) {
            line = content.getLineInformation(i);
            editorContentList.add(content.getText(line.getBeginOffset(), line.getLength()));
        }
        
        // Little trick to get uniques from an ArrayList
        if (unique)
            editorContentList = new ArrayList<String>(new HashSet<String>(editorContentList));

        // Handle various numeric cases
        if (numeric || binary || octal || hex) {
            NumericStringComparator nsc = null;
            if (binary)
            	nsc = new NumericStringComparator(BINARY_FLAG, pattern, usePatternR);
            else if (octal)
            	nsc = new NumericStringComparator(OCTAL_FLAG, pattern, usePatternR);
            else if (hex)
            	nsc = new NumericStringComparator(HEX_FLAG, pattern, usePatternR);
            else if (numeric)
            	nsc = new NumericStringComparator(NUMERIC_FLAG, pattern, usePatternR);

            List<String> numericList = new ArrayList<String>();
            List<String> nonNumericList = new ArrayList<String>();

            Pattern p = null;
            Matcher m = null;
            for (String candidate : editorContentList) {
                if (usePattern || usePatternR) {
                    p = Pattern.compile(pattern);
                    m = p.matcher(candidate);
                    
                    if(m.matches() && hasNumber(candidate))
                    	numericList.add(candidate);
                    else
                    	nonNumericList.add(candidate);
                }
                else if (hasNumber(candidate))
                    numericList.add(candidate);
                else
                    nonNumericList.add(candidate);
            }

            Collections.sort(numericList, nsc);
            editorContentList = new ArrayList<String>(nonNumericList);
            editorContentList.addAll(numericList);
        } else if (ignoreCase) {
            // This has no effect on a pattern if a pattern was given
            IgnoreCaseStringComparator icsc = new IgnoreCaseStringComparator();
            Collections.sort(editorContentList, icsc);
        } else
            Collections.sort(editorContentList);

        if (reversed)
            Collections.reverse(editorContentList);

        StringBuilder replacementText = new StringBuilder();
        int total = content.getNumberOfLines();
        int count = 0;
        String newline = editorAdaptor.getConfiguration().getNewLine();
        for (String editorLine : editorContentList) {
            ++count;
            replacementText.append(editorLine);
            if (count != total) //don't append newline on last line in file
            	replacementText.append(newline);
        }

        // Replace the contents of the range with the freshly sorted text
        editorAdaptor.getModelContent().replace(
        		startLine.getBeginOffset(),
        		endLine.getBeginOffset() - startLine.getBeginOffset(),
        		replacementText.toString()
		);
    }

	public TextOperation repetition() {
		return this;
	}
}