package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
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
//        if (selection.getContentType(editorAdaptor.getConfiguration()) != ContentType.TEXT_RECTANGLE)
//            throw new CommandExecutionException("BlockSelectionMotion can only be used with a block selection!");
        
        final CursorService cs = editorAdaptor.getCursorService();
        final TextContent mc = editorAdaptor.getModelContent();
        final Position pos = editorAdaptor.getPosition();
        final LineInformation line = mc.getLineInformationOfOffset(pos.getModelOffset());
        
        final Rect rect = BlockWiseSelection.getRect(mc, selection);
        if (gotoStart) {
            return cs.newPositionForModelOffset(line.getBeginOffset() + rect.left);
        } else {
            return cs.newPositionForModelOffset(line.getBeginOffset() + rect.width());
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
