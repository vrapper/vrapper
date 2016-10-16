package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.BlockWiseSelectionArea;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.TextBlock;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.Selection;

public class BlockSelectionMotion extends AbstractMotion {

    public static final BlockSelectionMotion COLUMN_START = new BlockSelectionMotion(true);
    public static final BlockSelectionMotion COLUMN_END = new BlockSelectionMotion(false);

    private final boolean gotoStart;

    private BlockSelectionMotion(final boolean gotoStart) {
        this.gotoStart = gotoStart;
    }

    @Override
    public BorderPolicy borderPolicy() {
        return BorderPolicy.EXCLUSIVE;
    }

    @Override
    public Motion withCount(final int count) {
        return this;
    }

    @Override
    public Position destination(final EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        final Selection selection = editorAdaptor.getSelection();
        if (selection == null)
            throw new CommandExecutionException("BlockSelectionMotion must have a selection!");
        
        final BlockWiseSelectionArea lastSel = (BlockWiseSelectionArea)
                editorAdaptor.getRegisterManager().getLastActiveSelectionArea();
        final CursorService cs = editorAdaptor.getCursorService();
        final TextContent mc = editorAdaptor.getModelContent();
        final TextRange tr = lastSel.getRegion(editorAdaptor, NO_COUNT_GIVEN);
        final TextBlock tb = BlockWiseSelection.getTextBlock(tr.getStart(), tr.getEnd(), mc, cs);
        if (gotoStart) {
            return cs.getPositionByVisualOffset(tb.startLine, tb.startVisualOffset);
        } else {
            Position pos = null;
            if (!lastSel.isUntilEOL()) {
                pos = cs.getPositionByVisualOffset(tb.startLine, tb.endVisualOffset);
            }
            final LineInformation line = mc.getLineInformation(tb.startLine);
            if (pos != null && pos.getModelOffset() < line.getEndOffset()) {
                return pos.addModelOffset(1);
            } else {
                return cs.newPositionForModelOffset(line.getEndOffset());
            }
        }
    }

    @Override
    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.NEVER;
    }

    @Override
    public boolean isJump() {
        return false;
    }
}
