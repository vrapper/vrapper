package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class UndoCommand extends SimpleRepeatableCommand {

    public static final UndoCommand INSTANCE = new UndoCommand();

    private UndoCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor) {
        editorAdaptor.getHistory().undo();
    }

    public CountAwareCommand repetition() {
        return null;
    }

}
