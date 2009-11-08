package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.commands.DelimitedTextObject;

public class OuterTextObject extends DelimitedTextObject {

    public OuterTextObject(DelimitedText delimitedText) {
        super(delimitedText);
    }

    @Override
    protected TextRange getRegion(TextRange leftDelimiter, TextRange rightDelimiter) {
        return new StartEndTextRange(leftDelimiter.getLeftBound(), rightDelimiter.getRightBound());
    }

}
