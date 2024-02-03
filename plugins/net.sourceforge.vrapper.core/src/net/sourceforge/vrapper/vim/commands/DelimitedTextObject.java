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

    public TextRange getRegion(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        Position currentPosition = editorAdaptor.getPosition();
        int currentOffset = currentPosition.getModelOffset();
        TextRange leftDelimiter = delimitedText.leftDelimiter(currentOffset, editorAdaptor, count);
        TextRange rightDelimiter = delimitedText.rightDelimiter(currentOffset, editorAdaptor, count);
        return getRegion(leftDelimiter, rightDelimiter);
    }

    protected abstract TextRange getRegion(TextRange leftDelimiter, TextRange rightDelimiter);

    public ContentType getContentType(Configuration configuration) {
        return ContentType.TEXT;
    }

}