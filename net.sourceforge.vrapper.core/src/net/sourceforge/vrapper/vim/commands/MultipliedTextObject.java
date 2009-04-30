package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class MultipliedTextObject implements TextObject {

    private final int count;
    private final TextObject textObject;

    public MultipliedTextObject(int count, TextObject textObject) {
        this.count = count;
        this.textObject = textObject;
    }

    public ContentType getContentType() {
        return textObject.getContentType();
    }

    public TextRange getRegion(EditorAdaptor editorMode, int count) throws CommandExecutionException {
        return textObject.getRegion(editorMode, count);
    }

    public TextObject withCount(int count) {
        return new MultipliedTextObject(count, textObject);
    }

    public int getCount() {
        return count;
    }

}
