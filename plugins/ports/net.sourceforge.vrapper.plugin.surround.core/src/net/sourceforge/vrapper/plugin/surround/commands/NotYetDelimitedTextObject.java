/**
 * 
 */
package net.sourceforge.vrapper.plugin.surround.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.commands.TextObject;

class NotYetDelimitedTextObject implements DelimitedText {
    private final TextObject textObject;

    public NotYetDelimitedTextObject(TextObject textObject) {
        this.textObject = textObject;
    }

    @Override
    public TextRange leftDelimiter(int offset, EditorAdaptor editorAdaptor,
            int count) throws CommandExecutionException {
        Position leftBound = textObject.getRegion(editorAdaptor, textObject.getCount()).getLeftBound();
        return new StartEndTextRange(leftBound, leftBound);
    }

    @Override
    public TextRange rightDelimiter(int offset, EditorAdaptor editorAdaptor,
            int count) throws CommandExecutionException {
        Position rightBound = textObject.getRegion(editorAdaptor, textObject.getCount()).getRightBound();
        return new StartEndTextRange(rightBound, rightBound);
    }
}