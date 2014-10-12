package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

/**
 * Wrapper command for motions in visual mode.
 *
 * @author Matthias Radig
 */
public class LinewiseVisualMotionCommand extends AbstractVisualMotionCommand {

    public LinewiseVisualMotionCommand(Motion motion) {
        super(motion);
    }

    @Override
    protected void extendSelection(EditorAdaptor editorAdaptor,
            Selection oldSelection, int motionCount) {
        Position from = oldSelection.getFrom();
        Position to = editorAdaptor.getPosition();
		editorAdaptor.setSelection(new LineWiseSelection(editorAdaptor, from, to));
    }
}
