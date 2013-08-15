package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class SimpleTextOperation implements TextOperation {

    public void execute(EditorAdaptor editorAdaptor, int count, TextObject textObject)
            throws CommandExecutionException {
        TextRange range = textObject.getRegion(editorAdaptor, count);
        
        execute(editorAdaptor, range, textObject.getContentType(editorAdaptor.getConfiguration()));
    }

    /**
     * Apply the operation on a textrange.
     * @param editorAdaptor editor reference.
     * @param region textrange, can be <tt>null</tt> if a motion didn't select any text.
     * @param contentType range content type.
     */
    public abstract void execute(EditorAdaptor editorAdaptor, TextRange region,
            ContentType contentType) throws CommandExecutionException;

}
