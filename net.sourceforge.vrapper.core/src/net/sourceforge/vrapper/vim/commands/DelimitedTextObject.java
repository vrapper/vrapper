package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class DelimitedTextObject extends AbstractTextObject {

    protected final DelimitedText delimitedText;

    public DelimitedTextObject(DelimitedText delimitedText) {
        this.delimitedText = delimitedText;
    }

    public TextRange getRegion(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
                TextRange leftDelimiter = delimitedText.leftDelimiter(editorAdaptor, count);
                TextRange rightDelimiter = delimitedText.rightDelimiter(editorAdaptor, count);
                return getRegion(leftDelimiter, rightDelimiter);
            }

    protected abstract TextRange getRegion(TextRange leftDelimiter, TextRange rightDelimiter);

    public ContentType getContentType(Configuration configuration) {
        return ContentType.TEXT;
    }

}