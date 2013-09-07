package net.sourceforge.vrapper.vim.commands.motions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * '%' movement for C Pre-Processor conditionals:
 * #if, #ifdef, #else, #elif, #endif
 * 
 * 'endif' always goes back to 'if' or 'ifdef', all other
 * conditionals move forward to the next 'else', 'elif' or 'endif'.
 */
public class CPreProcessorMove extends AbstractModelSideMotion {
    
    private static final Pattern preProcessorPattern = Pattern.compile("^\\s*#\\s*(if|ifdef|else|elif|endif)");
    private final String IF    = "if";
    private final String IFDEF = "ifdef";
    //private final String ELSE  = "else";
    //private final String ELIF  = "elif";
    private final String ENDIF = "endif";
    
    /**
     * Check to see if the cursor is on a pre-processor conditional
     */
    public static final boolean containsPreProcessor(final TextContent content, final LineInformation line, final int offset) {
        String atCursor = content.getText(offset, 1);
        //first pass, is the cursor on one of the possible characters?
        if("#\t ifdelsn".contains(atCursor)) {
            String lineStr = content.getText(line.getBeginOffset(), line.getLength());
            Matcher matcher = preProcessorPattern.matcher(lineStr);
            //second pass, does the line start with a pre-processor conditional?
            if(matcher.find()) {
                //third pass, is the cursor on or before the pre-processor conditional?
                if( (offset - line.getBeginOffset()) <= matcher.end()) {
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }
        }

        return false;
    }

    @Override
    protected int destination(int offset, TextContent content, int count) throws CommandExecutionException {
        LineInformation line = content.getLineInformationOfOffset(offset);
        String lineStr = content.getText(line.getBeginOffset(), line.getLength());
        String word = getPreProcessorWord(lineStr);

        if(ENDIF.equals(word)) { //search backwards
            return findIfDef(offset, content, count);
        }
        else { //search forwards
            return findConditional(offset, content, count);
        }
    }
    
    /**
     * Search backwards for the 'if' or 'ifdef' that corresponds
     * to the 'endif' under the cursor.  We can basically ignore
     * all 'else' and 'elif' we may run into.
     */
    private int findIfDef(int offset, TextContent content, int count) {
        int depth = count;
        LineInformation line = content.getLineInformationOfOffset(offset);
        int lineNo = line.getNumber() - 1; //start on the previous line
        String lineStr;

        while(lineNo > -1) {
            line = content.getLineInformation(lineNo);
            lineNo--; //prepare for next iteration

            if( ! CPreProcessorMove.containsPreProcessor(content, line, line.getBeginOffset())) {
                continue;
            }
            
            lineStr = content.getText(line.getBeginOffset(), line.getLength());
            String word = getPreProcessorWord(lineStr);
            if(ENDIF.equals(word)) {
                depth++;
            }
            else if(IF.equals(word) || IFDEF.equals(word)) {
                depth--;
            }

            if(depth == 0) {
                return lineStr.indexOf('#') + line.getBeginOffset();
            }
        }

        return offset;
    }
    
    /**
     * Find the next pre-processor line at the same depth as the conditional
     * under the cursor.  Depths are modified with each 'if' and 'endif', which
     * means 'else' and 'elif' are at the (depth + 1) of what we would usually
     * look for.
     */
    private int findConditional(int offset, TextContent content, int count) {
        int depth = count;
        LineInformation line = content.getLineInformationOfOffset(offset);
        int lineNo = line.getNumber() + 1; //start on the next line
        int totalLines = content.getNumberOfLines();
        String lineStr;

        while(lineNo < totalLines) {
            line = content.getLineInformation(lineNo);
            lineNo++; //prepare for next iteration

            if(line.getLength() == 0) {
                continue;
            }
            if( ! CPreProcessorMove.containsPreProcessor(content, line, line.getBeginOffset())) {
                continue;
            }
            
            lineStr = content.getText(line.getBeginOffset(), line.getLength());
            String word = getPreProcessorWord(lineStr);
            if(IF.equals(word) || IFDEF.equals(word)) {
                depth++;
            }
            else if(depth == 1) { //else and elif are at depth 1
                return lineStr.indexOf('#') + line.getBeginOffset();
            }
            else if(ENDIF.equals(word)) {
                depth--;
            }
        }

        return offset;
    }
    
    private String getPreProcessorWord(String line) {
        Matcher matcher = preProcessorPattern.matcher(line);
        matcher.find();
        return matcher.group(1);
    }

    @Override
    public BorderPolicy borderPolicy() {
        return BorderPolicy.INCLUSIVE;
    }

}
