package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class DummyTextObject implements TextObject {
    
    protected TextRange range;

    public DummyTextObject(TextRange range) {
        this.range = range;
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
        return ContentType.TEXT;
    }

}
