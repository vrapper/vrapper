package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class RedoCommand extends SimpleRepeatableCommand {

    public void execute(EditorAdaptor editorAdaptor) {
        editorAdaptor.getHistory().redo();
    }

    public CountAwareCommand repetition() {
        return null;
    }

}
