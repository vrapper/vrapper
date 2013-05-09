package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.PositionlessSelection;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.Rect;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.Selection;

public class BlockSelectionMotion implements Motion {

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
    public int getCount() {
        return NO_COUNT_GIVEN;
    }

    @Override
    public Position destination(final EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        final Selection selection = editorAdaptor.getSelection();
        if (selection == null)
            throw new CommandExecutionException("BlockSelectionMotion must have a selection!");
        
        final PositionlessSelection lastSel = editorAdaptor.getRegisterManager().getLastActiveSelection();
        final CursorService cs = editorAdaptor.getCursorService();
        final TextContent mc = editorAdaptor.getModelContent();
        
        final Rect rect = BlockWiseSelection.getRect(editorAdaptor, lastSel);
        final LineInformation line = mc.getLineInformation(rect.top);
        if (gotoStart) {
            return cs.newPositionForModelOffset(line.getBeginOffset() + rect.left);
        } else {
            return cs.newPositionForModelOffset(line.getBeginOffset() + rect.right + 1);
        }
    }

    @Override
    public boolean updateStickyColumn() {
        return false;
    }

    @Override
    public boolean isJump() {
        return false;
    }

}
