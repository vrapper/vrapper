package kg.totality.core.commands;

import de.jroene.vrapper.eclipse.VrapperPlugin;
import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.motions.Motion;
import kg.totality.core.modes.InsertMode;

public class ChangeToInsertModeCommand extends CountAwareCommand {

	private final Motion motion;

	public ChangeToInsertModeCommand() {
		motion = null;
	}

	public ChangeToInsertModeCommand(Motion motion) {
		this.motion = motion;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count) {
		if (count != NO_COUNT_GIVEN)
			VrapperPlugin.error("counted inserts not yet supported");
		if (motion != null)
			MotionCommand.doIt(editorAdaptor, motion);
		editorAdaptor.changeMode(InsertMode.NAME);
	}

	@Override
	public CountAwareCommand repetition() {
		return new RepeatLastInsertCommand(motion);
	}
}
