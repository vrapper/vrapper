package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
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
        CursorService cursorService = editorAdaptor.getCursorService();
        int docLen = editorAdaptor.getModelContent().getTextLength();
        // 'to' must be "on" the last character of the file, not behind it.
        // The case for an empty file is handled in shiftPositionForModelOffset()
        if (to.getModelOffset() == docLen) {
            to = cursorService.shiftPositionForModelOffset(to.getModelOffset(), -1, true);
        }
		editorAdaptor.setSelection(new LineWiseSelection(editorAdaptor, from, to));
    }
}
