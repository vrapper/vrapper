package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;

public class RestoreSelectionCommand extends CountIgnoringNonRepeatableCommand {
    
    public static final Command INSTANCE = new RestoreSelectionCommand();

    @Override
    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        Selection selection = editorAdaptor.getLastActiveSelection();
        if (selection != null) {
            editorAdaptor.changeMode(selection.getModeName(), AbstractVisualMode.RECALL_SELECTION_HINT);
        }
    }

}
