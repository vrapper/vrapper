package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class DeleteOperation extends SimpleTextOperation {

    public static final DeleteOperation INSTANCE = new DeleteOperation();

    private DeleteOperation() { /* NOP */ }

    @Override
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
        
        if (editorAdaptor.getFileService().isEditable()) {
            CursorService cursorService = editorAdaptor.getCursorService();
            TextContent txt = editorAdaptor.getModelContent();
            
            int position = range.getLeftBound().getModelOffset();
            int length = range.getModelLength();
            
            if (position + length == txt.getTextLength())
                VrapperLog.info("end-cut");
            
            txt.replace(position, length, "");
            
            // fix sticky column
            Position here = cursorService.getPosition();
            cursorService.setPosition(here, true);
        }
    }
}
