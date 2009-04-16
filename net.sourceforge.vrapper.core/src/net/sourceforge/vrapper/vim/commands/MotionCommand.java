/**
 *
 */
package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

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