package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/** FIXME Make sure this works in all cases */
public class BlockWiseSelection implements Selection {

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
    
    private static int getXOffset(final TextContent textContent, final int modelOffset) {
        final LineInformation info = textContent.getLineInformationOfOffset(modelOffset);
        return modelOffset - info.getBeginOffset();
    }
    
    private static int getLine(final TextContent textContent, final int modelOffset) {
        final LineInformation info = textContent.getLineInformationOfOffset(modelOffset);
        return info.getNumber();
    }

    public static int getX(final TextContent textContent, final Selection selection) {
        final int modelOffset = selection.getStart().getModelOffset();
        return getXOffset(textContent, modelOffset);
    }

    public static int getY(final TextContent textContent, final Selection selection) {
        return getLine(textContent, selection.getStart().getModelOffset());
    }
    
    public static int getWidth(final TextContent textContent, final Selection selection) {
        final int leftX = getX(textContent, selection);
        final int rightX = getXOffset(textContent, selection.getEnd().getModelOffset());
        return rightX - leftX + 1;
    }

    public static int getHeight(final TextContent textContent, final Selection selection) {
        final int top = getY(textContent, selection);
        final int bottom = getLine(textContent, selection.getEnd().getModelOffset());
        return bottom - top + 1;
    }


}
