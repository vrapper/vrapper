package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class CloseCommand extends CountIgnoringNonRepeatableCommand {

    public static final CloseCommand FORCED_CLOSE = new CloseCommand(true);
    public static final CloseCommand CLOSE = new CloseCommand(false);
    private final boolean force;

    private CloseCommand(boolean force) {
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
