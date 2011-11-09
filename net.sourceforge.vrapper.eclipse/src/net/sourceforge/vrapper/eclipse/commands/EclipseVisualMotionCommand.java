package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractVisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
import net.sourceforge.vrapper.vim.commands.LinewiseVisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.modes.LinewiseVisualMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;

public class EclipseVisualMotionCommand extends CountIgnoringNonRepeatableCommand {

	private Motion m;
	
	public EclipseVisualMotionCommand(String action) {
		m = new EclipseMoveCommand(action, BorderPolicy.INCLUSIVE);
	}
	
	public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
		try {
			String mode = editorAdaptor.getCurrentModeName();
			AbstractVisualMotionCommand cmd;
			if (LinewiseVisualMode.NAME.equals(mode)) {
				cmd = new LinewiseVisualMotionCommand(m);
			} else if (VisualMode.NAME.equals(mode)) {
				cmd = new VisualMotionCommand(m);
			} else {
				return;
			}
			cmd.execute(editorAdaptor);
		} finally {
			editorAdaptor.getViewportService().setRepaint(true);
		}
	}

}
