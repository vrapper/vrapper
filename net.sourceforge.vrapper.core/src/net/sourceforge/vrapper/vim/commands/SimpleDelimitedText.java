package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.AbstractModelSideMotion;
import net.sourceforge.vrapper.vim.commands.motions.BailOffMotion;
import net.sourceforge.vrapper.vim.commands.motions.CountAwareMotion;
import net.sourceforge.vrapper.vim.commands.motions.FindBalancedMotion;

public class SimpleDelimitedText implements DelimitedText {

    private AbstractModelSideMotion leftMotion;
    private AbstractModelSideMotion rightMotion;

    public SimpleDelimitedText(char leftDelim, char rightDelim) {
        leftMotion = new BailOffMotion(leftDelim, new FindBalancedMotion(leftDelim, rightDelim, true, true));
        rightMotion = new BailOffMotion(rightDelim, new FindBalancedMotion(rightDelim, leftDelim, true, false));
    }

    public SimpleDelimitedText(char delimiter) {
        leftMotion = new FindBalancedMotion(delimiter, '\0', true, true);
        rightMotion = new BailOffMotion(delimiter, new FindBalancedMotion(delimiter, '\0', true, false));
    }

    /**
     * Vim doesn't start a delimited range on a newline or end a range on an
     * empty line (try 'vi{' while within a function for proof).  So, define the
     * range for delimiters to include the character on one end but stop at the
     * newline boundary at the other end.  This is to handle the difference
     * between 'i' and 'a' for delimited text objects.
     */
    public TextRange leftDelimiter(int offset, EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        Position left = leftMotion.destination(offset, editorAdaptor, count);
        Position endDelim = left.addModelOffset(1);
        
        TextContent content = editorAdaptor.getModelContent();
        //is character after delimiter a newline?
        if(VimUtils.isNewLine(content.getText(left.getModelOffset() + 1, 1))) {
            //start after newline
            LineInformation line = content.getLineInformationOfOffset(left.getModelOffset());
            LineInformation nextLine = content.getLineInformation(line.getNumber() +1);
            endDelim = editorAdaptor.getCursorService().newPositionForModelOffset(nextLine.getBeginOffset());
        }
        
        return new StartEndTextRange(left, endDelim);
    }

    public TextRange rightDelimiter(int offset, EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        Position right = rightMotion.destination(offset, editorAdaptor, count);
        
        TextContent content = editorAdaptor.getModelContent();
        int startIndex = right.getModelOffset();
        LineInformation line = content.getLineInformationOfOffset(startIndex);
        int lineStart = line.getBeginOffset();
        
        Position startDelim = right;
        if(startIndex > lineStart) {
            //is everything before the delimiter just whitespace?
            String text = content.getText(lineStart, startIndex - lineStart);
            if(VimUtils.isBlank(text)) {
                //end on previous line
                LineInformation previousLine = content.getLineInformation(line.getNumber() -1);
                startIndex = previousLine.getEndOffset();
                startDelim = editorAdaptor.getCursorService().newPositionForModelOffset(startIndex);
            }
        }
        
        return new StartEndTextRange(startDelim, right.addModelOffset(1));
    }

}
