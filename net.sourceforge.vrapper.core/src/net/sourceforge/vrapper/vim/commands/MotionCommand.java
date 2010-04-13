/**
 *
 */
package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.ViewPortInformation;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class MotionCommand extends CountAwareCommand {

	protected final Motion motion;

	public MotionCommand(Motion motion) {
		this.motion = motion;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
		doIt(editorAdaptor, motion.withCount(count));
	}

	public static void doIt(EditorAdaptor editorAdaptor, Motion motion) throws CommandExecutionException {
		final Position destination = motion.destination(editorAdaptor);
        Position previousPosition = editorAdaptor.getPosition();
        gotoAndChangeViewPort(editorAdaptor, destination, motion.updateStickyColumn());
	    if (motion.isJump()) {
            editorAdaptor.getCursorService().setMark(CursorService.LAST_JUMP_MARK, previousPosition);
	    }
	}

    /**
     * Moves the cursor to the given position and centers the cursor line
     * if it is not inside the viewport.
     */
    public static void gotoAndChangeViewPort(EditorAdaptor editorAdaptor, Position pos, boolean updateStickyColumn) {
		if (pos.getViewOffset() < 0) {
            editorAdaptor.getViewportService().exposeModelPosition(pos);
        }
        LineInformation line = editorAdaptor.getViewContent().getLineInformationOfOffset(pos.getViewOffset());
        ViewportService viewportService = editorAdaptor.getViewportService();
        ViewPortInformation view = viewportService.getViewPortInformation();
        // center line if necessary
        int lineNo = line.getNumber();
        int top = view.getTopLine();
        int bottom = view.getBottomLine();
        int centerThreshold = view.getNumberOfLines()/2;
        if (lineNo >= bottom + centerThreshold || lineNo <= top - centerThreshold) {
            // center line
            CenterLineCommand.CENTER.doIt(editorAdaptor, lineNo);
        }
        editorAdaptor.getCursorService().setPosition(pos, updateStickyColumn);
    }


	@Override
	public CountAwareCommand repetition() {
		return null;
	}

}