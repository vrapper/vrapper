package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class RedoCommand extends SimpleRepeatableCommand {

    public static final RedoCommand INSTANCE = new RedoCommand();

    private RedoCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor) {
        editorAdaptor.getHistory().redo();
    }

    public CountAwareCommand repetition() {
        return null;
    }

}
