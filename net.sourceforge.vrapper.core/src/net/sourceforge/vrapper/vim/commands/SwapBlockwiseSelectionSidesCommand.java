package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.TextBlock;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public class SwapBlockwiseSelectionSidesCommand extends CountIgnoringNonRepeatableCommand {

    private enum SwapMode {
        DIAGONAL,
        HORIZONTAL
    };

    final SwapMode mode;

    public static final SwapBlockwiseSelectionSidesCommand DIAGONAL_INSTANCE
        = new SwapBlockwiseSelectionSidesCommand(SwapMode.DIAGONAL);
    public static final SwapBlockwiseSelectionSidesCommand HORIZONTAL_INSTANCE
        = new SwapBlockwiseSelectionSidesCommand(SwapMode.HORIZONTAL);

    private SwapBlockwiseSelectionSidesCommand(SwapMode mode) {
        this.mode = mode;
    }

    public void execute(EditorAdaptor editorAdaptor) {
        final CursorService cs = editorAdaptor.getCursorService();
        final TextContent mc = editorAdaptor.getModelContent();
        final Selection sel = editorAdaptor.getSelection();
        if (sel.getModelLength() == 1) {
            // do nothing
            return;
        }
        // Swap from and to offsets.
        Position newStartPos = sel.getTo();
        Position newEndPos =   sel.getFrom();
        switch (mode) {
        case DIAGONAL:
            // Swapping to and from is enough for the diagonal swap.
            break;
        case HORIZONTAL:
            final int startVOffs = cs.getVisualOffset(sel.getStart());
            final int endVOffs = cs.getVisualOffset(sel.getEnd());
            final LineInformation startLine = mc.getLineInformationOfOffset(sel.getStart().getModelOffset());
            final LineInformation endLine = mc.getLineInformationOfOffset(sel.getEnd().getModelOffset());
            // Swap visual offsets of the start and end position, but keep them
            // on the same line.
            newStartPos = cs.getPositionByVisualOffset(startLine.getNumber(), endVOffs);
            newEndPos   = cs.getPositionByVisualOffset(endLine.getNumber(),   startVOffs);
            // Clamp by the last character on the line if there are no characters
            // at the visual offset (mimics VIM behaviour).
            if (newStartPos == null) {
                final int lineEndOfs = Math.max(startLine.getEndOffset(),
                        startLine.getBeginOffset());
                newStartPos = cs.newPositionForModelOffset(lineEndOfs);
            }
            if (newEndPos == null) {
                final int lineEndOfs = Math.max(endLine.getEndOffset(),
                        endLine.getBeginOffset());
                newEndPos = cs.newPositionForModelOffset(lineEndOfs);
            }
            break;
        }
        MotionCommand.gotoAndChangeViewPort(editorAdaptor, newEndPos, StickyColumnPolicy.ON_CHANGE);
        editorAdaptor.setSelection(sel.reset(editorAdaptor, newStartPos, newEndPos));
    }
}
