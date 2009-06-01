package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SwapSelectionSidesCommand extends CountIgnoringNonRepeatableCommand {

    public void execute(EditorAdaptor editorAdaptor) {
        Selection selection = editorAdaptor.getSelection();
        editorAdaptor.setPosition(selection.getEnd(), true);
        editorAdaptor.setSelection(new Selection(
                new StartEndTextRange(selection.getEnd(), selection.getStart()),
                selection.getContentType()));
    }

}
