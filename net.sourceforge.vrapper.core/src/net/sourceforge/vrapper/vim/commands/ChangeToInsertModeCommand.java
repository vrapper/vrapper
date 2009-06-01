package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.modes.InsertMode;

public class ChangeToInsertModeCommand extends CountAwareCommand {

	private final Motion motion;

	public ChangeToInsertModeCommand() {
		motion = null;
	}

	public ChangeToInsertModeCommand(Motion motion) {
		this.motion = motion;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
		if (count != NO_COUNT_GIVEN) {
            VrapperLog.error("counted inserts not yet supported");
        }
		if (motion != null) {
            MotionCommand.doIt(editorAdaptor, motion);
        }
		editorAdaptor.changeMode(InsertMode.NAME);
	}

	@Override
	public CountAwareCommand repetition() {
		return new RepeatLastInsertCommand(motion);
	}
}
