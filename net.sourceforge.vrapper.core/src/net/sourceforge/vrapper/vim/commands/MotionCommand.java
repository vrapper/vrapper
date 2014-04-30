/**
 *
 */
package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.ViewPortInformation;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public class MotionCommand extends CountAwareCommand {

    private final Motion motion;

    public MotionCommand(Motion motion) {
        this.motion = motion;
    }

    public Motion getMotion(int count) {
        return motion.withCount(count);
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        doIt(editorAdaptor, motion.withCount(count));
    }

    @Override
    public int getCount() {
        return motion.getCount();
    }

	public static void doIt(EditorAdaptor editorAdaptor, Motion motion) throws CommandExecutionException {
		final Position destination = motion.destination(editorAdaptor);
        Position previousPosition = editorAdaptor.getPosition();
        gotoAndChangeViewPort(editorAdaptor, destination, motion.stickyColumnPolicy());
	    if (motion.isJump()) {
            editorAdaptor.getCursorService().setMark(CursorService.LAST_JUMP_MARK, previousPosition);
	    }
	}

    /**
     * Moves the cursor to the given position and centers the cursor line
     * if it is not inside the viewport.
     */
    public static void gotoAndChangeViewPort(EditorAdaptor editorAdaptor, Position pos, StickyColumnPolicy stickyColumnPolicy) {
		if (pos.getViewOffset() < 0) {
            editorAdaptor.getViewportService().exposeModelPosition(pos);
        }
        TextContent viewContent = editorAdaptor.getViewContent();
        LineInformation line = viewContent.getLineInformationOfOffset(pos.getViewOffset());
        ViewportService viewportService = editorAdaptor.getViewportService();
        ViewPortInformation view = viewportService.getViewPortInformation();
        int scrollOff = editorAdaptor.getConfiguration().get(Options.SCROLL_OFFSET);
        int scrollJump = editorAdaptor.getConfiguration().get(Options.SCROLL_JUMP);
        int lineNo = line.getNumber();
        int top = view.getTopLine()+scrollOff;
        int bottom = view.getBottomLine()-scrollOff;
        int centerThreshold = view.getNumberOfLines()/2;
        scrollJump = Math.min(bottom-top, scrollJump);
        if (lineNo >= bottom + centerThreshold || lineNo <= top - centerThreshold || scrollOff >= centerThreshold) {
            // center line
            CenterLineCommand.CENTER.doIt(editorAdaptor, lineNo);
        } else if (lineNo > bottom) {
            int jumpLineNo = Math.min(lineNo+scrollJump-1, viewContent.getNumberOfLines() - 1);
            CenterLineCommand.BOTTOM.doIt(editorAdaptor, jumpLineNo);
        } else if (lineNo < top) {
            int jumpLineNo = Math.max(lineNo-scrollJump+1, 0);
            CenterLineCommand.TOP.doIt(editorAdaptor, jumpLineNo);
        }
        editorAdaptor.getCursorService().setPosition(pos, stickyColumnPolicy);
    }


	@Override
	public CountAwareCommand repetition() {
		return null;
	}

}