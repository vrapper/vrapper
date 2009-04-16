/**
 *
 */
package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.motions.Motion;
import newpackage.position.Position;

public class MotionCommand extends CountAwareCommand {
	protected final Motion motion;

	public MotionCommand(Motion motion) {
		this.motion = motion;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count) {
		doIt(editorAdaptor, motion.withCount(count));
	}

	public static void doIt(EditorAdaptor editorAdaptor, Motion motion) {
		final Position destination = motion.destination(editorAdaptor);
		editorAdaptor.setPosition(destination, motion.updateStickyColumn());
	}

	@Override
	public CountAwareCommand repetition() {
		return null;
	}

}