package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SaveAllCommand extends CountIgnoringNonRepeatableCommand {


    public static final SaveAllCommand INSTANCE = new SaveAllCommand();

    private SaveAllCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        editorAdaptor.getWorkbenchService().saveAll();
    }

}
