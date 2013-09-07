package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class DelimitedTextObject extends AbstractTextObject {

    protected final DelimitedText delimitedText;

    public DelimitedTextObject(DelimitedText delimitedText) {
        this.delimitedText = delimitedText;
    }

    public TextRange getRegion(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        Selection currentSelection = editorAdaptor.getSelection();
        Position currentPosition = editorAdaptor.getPosition();
        int currentOffset = currentPosition.getModelOffset();
        TextRange leftDelimiter = delimitedText.leftDelimiter(currentOffset, editorAdaptor, count);
        TextRange rightDelimiter = delimitedText.rightDelimiter(currentOffset, editorAdaptor, count);
        final Position leftBound = leftDelimiter.getLeftBound();
        final Position rightBound = rightDelimiter.getRightBound();
        /* 
         * From VIM's search.c:
         * In Visual mode, when the resulting area is not bigger than what we
         * started with, extend it to the next block.
         */
        final int leftOffset = leftBound.getModelOffset();
        final int rightOffset = rightBound.getModelOffset();
        if (leftOffset >= currentSelection.getLeftBound().getModelOffset() &&
                leftOffset > 0 &&
                rightOffset <= currentSelection.getRightBound().getModelOffset() &&
                rightOffset < editorAdaptor.getModelContent().getTextLength()) {
            leftDelimiter = delimitedText.leftDelimiter(leftOffset - 1, editorAdaptor, count);
            rightDelimiter = delimitedText.rightDelimiter(rightOffset, editorAdaptor, count);
        }
        return getRegion(leftDelimiter, rightDelimiter);
    }

    protected abstract TextRange getRegion(TextRange leftDelimiter, TextRange rightDelimiter);

    public ContentType getContentType(Configuration configuration) {
        return ContentType.TEXT;
    }

}