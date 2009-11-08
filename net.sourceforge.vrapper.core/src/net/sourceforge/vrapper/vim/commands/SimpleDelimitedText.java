package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.BailOffMotion;
import net.sourceforge.vrapper.vim.commands.motions.CountAwareMotion;
import net.sourceforge.vrapper.vim.commands.motions.FindBalancedMotion;

public class SimpleDelimitedText implements DelimitedText {

    private CountAwareMotion leftMotion;
    private CountAwareMotion rightMotion;

    public SimpleDelimitedText(char leftDelim, char rightDelim) {
        leftMotion = new BailOffMotion(leftDelim, new FindBalancedMotion(leftDelim, rightDelim, true, true));
        rightMotion = new BailOffMotion(rightDelim, new FindBalancedMotion(rightDelim, leftDelim, true, false));
    }

    public SimpleDelimitedText(char delimiter) {
        leftMotion = new FindBalancedMotion(delimiter, '\0', true, true);
        rightMotion = new BailOffMotion(delimiter, new FindBalancedMotion(delimiter, '\0', true, false));
    }

    public TextRange leftDelimiter(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        Position left = leftMotion.destination(editorAdaptor, count);
        return new StartEndTextRange(left, left.addModelOffset(1));
    }

    public TextRange rightDelimiter(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        Position right = rightMotion.destination(editorAdaptor, count);
        return new StartEndTextRange(right, right.addModelOffset(1));
    }

}
