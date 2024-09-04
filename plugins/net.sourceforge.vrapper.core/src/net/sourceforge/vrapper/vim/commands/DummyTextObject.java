package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class DummyTextObject extends AbstractTextObject {
    
    protected TextRange range;
    protected ContentType contentType = ContentType.TEXT;

    public DummyTextObject(TextRange range) {
        this.range = range;
    }

    public DummyTextObject(TextRange range, ContentType contentType) {
        this.range = range;
        this.contentType = contentType;
    }

    @Override
    public TextObject withCount(int count) {
        return this;
    }

    @Override
    public int getCount() {
        return NO_COUNT_GIVEN;
    }

    @Override
    public TextRange getRegion(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        return range;
    }

    @Override
    public ContentType getContentType(Configuration configuration) {
        return contentType;
    }

}
