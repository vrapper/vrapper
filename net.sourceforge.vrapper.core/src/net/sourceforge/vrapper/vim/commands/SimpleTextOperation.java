package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class SimpleTextOperation implements TextOperation {

    public void execute(EditorAdaptor editorAdaptor, int count, TextObject textObject)
            throws CommandExecutionException {
        execute(editorAdaptor, textObject.getRegion(editorAdaptor, count), textObject.getContentType(editorAdaptor.getConfiguration()));
    }

    public abstract void execute(EditorAdaptor editorAdaptor, TextRange region,
            ContentType contentType) throws CommandExecutionException;

}
