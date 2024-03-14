package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.AbstractModelSideMotion;
import net.sourceforge.vrapper.vim.commands.motions.BailOffMotion;
import net.sourceforge.vrapper.vim.commands.motions.FindBalancedMotion;

public class SimpleDelimitedText implements DelimitedText {

    private AbstractModelSideMotion leftMotion;
    private AbstractModelSideMotion rightMotion;

    public SimpleDelimitedText(char leftDelim, char rightDelim) {
        leftMotion = new BailOffMotion(leftDelim, new FindBalancedMotion(leftDelim, rightDelim, true, true, false));
        rightMotion = new BailOffMotion(rightDelim, new FindBalancedMotion(rightDelim, leftDelim, true, false, false));
    }

    public SimpleDelimitedText(char delimiter) {
        leftMotion = new FindBalancedMotion(delimiter, '\0', true, true, false);
        rightMotion = new BailOffMotion(delimiter, new FindBalancedMotion(delimiter, '\0', true, false, false));
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

        Position leftDelim = VimUtils.fixLeftDelimiter(
                editorAdaptor.getModelContent(),
                editorAdaptor.getCursorService(),
                left);
        
        return new StartEndTextRange(left, leftDelim);
    }

    public TextRange rightDelimiter(int offset, EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        Position right = rightMotion.destination(offset, editorAdaptor, count);

        Position rightDelim = VimUtils.fixRightDelimiter(
                editorAdaptor.getModelContent(),
                editorAdaptor.getCursorService(),
                right);
        
        return new StartEndTextRange(rightDelim, right.addModelOffset(1));
    }

}
