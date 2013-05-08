package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/** FIXME Make sure this works in all cases */
public class BlockWiseSelection implements Selection {
    
    public static class Rect {
        public int left, top, right, bottom;
        
        public int width() {
            return right - left;
        }
        
        public int height() {
            return bottom - top + 1; // same row should be "1" height
        }
        
        @Override
        public String toString() {
            return String.format("%d %d - %d %d", left, top, right, bottom);
        }

        public Position getULPosition(final EditorAdaptor editorAdaptor) {
            final CursorService cs = editorAdaptor.getCursorService();
            final TextContent textContent = editorAdaptor.getModelContent();
            final int begin = textContent.getLineInformation(top).getBeginOffset();
            return cs.newPositionForModelOffset(begin + left);
        }
    }

    private final Position from;
    private final Position to;
    private final TextRange range;

    public BlockWiseSelection(final EditorAdaptor editor, final Position from, final Position to) {
        super();
        this.from = from;
        this.to = to;
        this.range = StartEndTextRange.exclusive(from, to);
    }

    @Override
    public TextRange getRegion(final EditorAdaptor editorMode, final int count)
            throws CommandExecutionException {
        return range;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public TextObject withCount(final int count) {
        return this;
    }

    @Override
    public Position getEnd() {
        return range.getEnd();
    }

    @Override
    public Position getLeftBound() {
        return range.getLeftBound();
    }

    @Override
    public int getModelLength() {
        return range.getModelLength();
    }

    @Override
    public Position getRightBound() {
        return range.getRightBound();
    }

    @Override
    public Position getStart() {
        return range.getStart();
    }

    @Override
    public int getViewLength() {
        return range.getViewLength();
    }

    @Override
    public boolean isReversed() {
        return range.isReversed();
    }
    @Override
    public ContentType getContentType(final Configuration configuration) {
        return ContentType.TEXT_RECTANGLE;
    }
    @Override
    public Position getFrom() {
        return from;
    }

    @Override
    public Position getTo() {
        return to;
    }
    
    public static Rect getRect(final TextContent textContent, final Selection selection) {
        final Rect ret = new Rect();
        final int fromX = VimUtils.calculateColForPosition(textContent, selection.getFrom());
        final int fromY = VimUtils.calculateLine(textContent, selection.getFrom());
        final int toX = VimUtils.calculateColForPosition(textContent, selection.getTo());
        final int toY = VimUtils.calculateLine(textContent, selection.getTo());
        
        ret.left  = Math.min(toX, fromX);
        ret.top   = Math.min(toY, fromY);
        ret.right = Math.max(toX, fromX);
        ret.bottom= Math.max(toY, fromY);
        
        return ret;
    }

}
