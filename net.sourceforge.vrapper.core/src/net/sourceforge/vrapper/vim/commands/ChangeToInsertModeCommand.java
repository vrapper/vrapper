package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.InsertMode;

public class ChangeToInsertModeCommand extends CountAwareCommand {

    private final Command command;

    public ChangeToInsertModeCommand() {
        this(null);
    }

	public ChangeToInsertModeCommand(Command command) {
        this.command = command;
    }

    @Override
	public void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
  		Integer i = Integer.valueOf(count);
  		editorAdaptor.changeMode(InsertMode.NAME, command, i);
	}

	@Override
	public CountAwareCommand repetition() {
	    return null;
	}
}
