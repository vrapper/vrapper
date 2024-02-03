package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.commandline.SearchMode;
import net.sourceforge.vrapper.vim.modes.commandline.SearchMode.Direction;

public class ChangeToSearchModeCommand extends CountAwareCommand {

	private boolean backwards;
	private Command executeOnCompletion;
	private boolean fromVisual;
	
	
	public ChangeToSearchModeCommand(boolean backwards, Command executeOnCompletion) {
		this.backwards = backwards;
		this.executeOnCompletion = executeOnCompletion;
	}

	public ChangeToSearchModeCommand(boolean backwards, Command executeOnCompletion, boolean fromVisual) {
		this.backwards = backwards;
		this.executeOnCompletion = executeOnCompletion;
		this.fromVisual = fromVisual;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count)
			throws CommandExecutionException {
		Direction direction = backwards ? SearchMode.Direction.BACKWARD : SearchMode.Direction.FORWARD;

		ExecuteCommandHint.OnLeave onLeaveCmd = new ExecuteCommandHint.OnLeave(executeOnCompletion.withCount(count));
		if (fromVisual) {
			editorAdaptor.changeMode(SearchMode.NAME, direction, SearchMode.FROM_VISUAL, onLeaveCmd);
		} else {
			editorAdaptor.changeMode(SearchMode.NAME, direction, onLeaveCmd);
		}
	}

	@Override
	public CountAwareCommand repetition() {
		return null;
	}

}
