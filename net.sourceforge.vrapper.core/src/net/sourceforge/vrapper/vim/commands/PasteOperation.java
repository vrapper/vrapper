package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.register.RegisterContent;

public class PasteOperation extends SimpleTextOperation {

    public static final PasteOperation INSTANCE = new PasteOperation();

    private PasteOperation() { /* NOP */
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, TextRange region,
            ContentType contentType) {
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

    public static void doIt(EditorAdaptor editorAdaptor, TextRange range,
            ContentType contentType) {
        RegisterContent registerContent = editorAdaptor.getRegisterManager().getActiveRegister().getContent();
        String text = registerContent.getText();

        ContentType pastingContentType = registerContent.getPayloadType();
        ContentType selectionContentType = editorAdaptor.getSelection().getContentType(editorAdaptor.getConfiguration());

        TextContent content = editorAdaptor.getModelContent();
        int offset = range.getLeftBound().getModelOffset();

        DeleteOperation.doIt(editorAdaptor, range, contentType);

        int position;
        if (selectionContentType == ContentType.LINES || pastingContentType == ContentType.LINES) {
            if (pastingContentType != ContentType.LINES) {
                text = text + "\n";
            } else if (selectionContentType != ContentType.LINES) {
                text = "\n" + text;
            }
            content.replace(offset, 0, text);
            LineInformation firstPastedLine = content.getLineInformationOfOffset(offset + 1);
            position = VimUtils.getFirstNonWhiteSpaceOffset(content, firstPastedLine);
        } else {
            position = offset + text.length() - 1;
            content.replace(offset, 0, text);
        }
        // content.replace(offset, 0, StringUtils.multiply(text, count));
        Position destination = editorAdaptor.getCursorService().newPositionForModelOffset(position);
        editorAdaptor.setPosition(destination, true);
    }
}
