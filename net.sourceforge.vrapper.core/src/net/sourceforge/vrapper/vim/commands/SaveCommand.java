package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SaveCommand extends CountIgnoringNonRepeatableCommand {

    public static final SaveCommand INSTANCE = new SaveCommand();

    private SaveCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        editorAdaptor.getFileService().save();
    }
}