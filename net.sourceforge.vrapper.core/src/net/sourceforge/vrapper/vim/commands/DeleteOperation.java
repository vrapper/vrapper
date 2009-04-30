package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class DeleteOperation implements TextOperation {

    public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType) {
        try {
            editorAdaptor.getHistory().beginCompoundChange();
            doIt(editorAdaptor, region, contentType);
        } finally {
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

    public TextOperation repetition() {
        return this;
    }

    public static void doIt(EditorAdaptor editorAdaptor, TextRange range, ContentType contentType) {
        YankOperation.doIt(editorAdaptor, range, contentType);
        TextContent content = editorAdaptor.getModelContent();
        if (editorAdaptor.getFileService().isEditable()) {
            content.replace(range.getLeftBound().getModelOffset(), range.getModelLength(), "");
        }
    }
}
