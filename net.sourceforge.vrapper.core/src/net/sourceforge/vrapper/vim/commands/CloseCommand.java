package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class CloseCommand extends CountIgnoringNonRepeatableCommand {

    private final boolean force;

    public CloseCommand(boolean force) {
        super();
        this.force = force;
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        if (!editorAdaptor.getFileService().close(force)) {
            throw new CommandExecutionException("There are unsaved changes");
        }
    }

}
