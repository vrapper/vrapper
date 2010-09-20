package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandOnEnterHint;
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
        if (command != null)
      		editorAdaptor.changeMode(InsertMode.NAME, new ExecuteCommandOnEnterHint(command),
      		                                          new InsertMode.WithCountHint(count));
        else
            editorAdaptor.changeMode(InsertMode.NAME, new InsertMode.WithCountHint(count));
	}

	@Override
	public CountAwareCommand repetition() {
	    return new RepeatInsertionCommand(command);
	}
}
