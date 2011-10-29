package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.commandline.SearchMode;
import net.sourceforge.vrapper.vim.modes.commandline.SearchMode.Direction;

public class ChangeToSearchModeCommand extends CountAwareCommand {

	private boolean backwards;
	private Command executeOnCompletion;
	
	
	public ChangeToSearchModeCommand(boolean backwards, Command executeOnCompletion) {
		this.backwards = backwards;
		this.executeOnCompletion = executeOnCompletion;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count)
			throws CommandExecutionException {
		Direction direction = backwards ? SearchMode.Direction.BACKWARD : SearchMode.Direction.FORWARD;
		editorAdaptor.changeMode(SearchMode.NAME, direction,
				new ExecuteCommandHint.OnLeave(executeOnCompletion.withCount(count)));
	}

	@Override
	public CountAwareCommand repetition() {
		return null;
	}

}
