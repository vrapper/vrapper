package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;


public class InnerTextObject extends DelimitedTextObject {

    public InnerTextObject(DelimitedText delimitedText) {
        super(delimitedText);
    }

    @Override
    protected StartEndTextRange getRegion(TextRange leftDelimiter, TextRange rightDelimiter) {
        return new StartEndTextRange(leftDelimiter.getRightBound(), rightDelimiter.getLeftBound());
    }
}