package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
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
            
            boolean deletesLastLine = contentType == ContentType.LINES && range.getEnd().getModelOffset() == txt.getTextLength();
            
            int position = range.getLeftBound().getModelOffset();
            int length = range.getModelLength();
            
            if (deletesLastLine && position > 0) {
                position -= 1;
                length += 1;
            }
            
            txt.replace(position, length, "");
            
            // don't move cursor
            Position newPosition = cursorService.getPosition();
            
            if (deletesLastLine) {
                LineInformation lastLine = txt.getLineInformationOfOffset(newPosition.getModelOffset());
                newPosition = newPosition.setModelOffset(lastLine.getBeginOffset());
            }
            
            cursorService.setPosition(newPosition, true);
        }
    }
}
