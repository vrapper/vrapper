package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Special command implementation which either does nothing or only raises a user error.
 */
public class DummyCommand extends CountIgnoringNonRepeatableCommand {
    
    /** A command which does nothing. */
    public static final Command INSTANCE = new DummyCommand();

    private String failureMessage;

    protected DummyCommand() {
    }

    public DummyCommand(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        if (failureMessage != null) {
            throw new CommandExecutionException(failureMessage);
        }
        // else we do nothing
    }
}
