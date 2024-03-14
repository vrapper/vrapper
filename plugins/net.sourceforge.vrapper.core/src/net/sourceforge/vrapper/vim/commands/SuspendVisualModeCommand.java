package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

/** Remembers the last active selection for later use. */
public class SuspendVisualModeCommand extends CountIgnoringNonRepeatableCommand {

    public static final Command INSTANCE = new SuspendVisualModeCommand();

    private SuspendVisualModeCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        // Only save selection when this command is executed - other commands call doIt() as well.
        editorAdaptor.rememberLastActiveSelection();
    }
}
