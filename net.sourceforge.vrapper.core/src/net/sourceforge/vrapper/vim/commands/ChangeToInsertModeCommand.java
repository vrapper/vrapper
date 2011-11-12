package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.WithCountHint;

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
      		editorAdaptor.changeMode(InsertMode.NAME, new ExecuteCommandHint.OnEnter(command),
      		                                          new WithCountHint(count));
        else
            editorAdaptor.changeMode(InsertMode.NAME, new WithCountHint(count));
	}

	@Override
	public CountAwareCommand repetition() {
	    return new RepeatInsertionCommand(command);
	}
}
