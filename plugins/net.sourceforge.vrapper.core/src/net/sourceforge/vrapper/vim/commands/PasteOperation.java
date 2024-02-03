package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.register.RegisterContent;

/**
 * Replaces a range of text matched by a TextObject with the contents of a register.
 */
public class PasteOperation implements TextOperation {

    public static final PasteOperation INSTANCE = new PasteOperation(true);
    public static final PasteOperation INSTANCE_TEMPVISUAL = new PasteOperation(false);

    private final boolean fixNormalModeCursor;

    private PasteOperation(boolean fixNormalCursor) {
        fixNormalModeCursor = fixNormalCursor;
    }

    public void execute(EditorAdaptor editorAdaptor, int count,
            TextObject textObject) throws CommandExecutionException {
        try {
            editorAdaptor.getHistory().beginCompoundChange();
            doIt(editorAdaptor, count,
                    textObject.getRegion(editorAdaptor, Command.NO_COUNT_GIVEN),
                    textObject.getContentType(editorAdaptor.getConfiguration()));
        } finally {
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

    public TextOperation repetition() {
        return this;
    }

    protected void doIt(EditorAdaptor editorAdaptor, int count, TextRange range, ContentType contentType) {
        if (count == Command.NO_COUNT_GIVEN)
            count = 1;
        RegisterContent registerContent = editorAdaptor.getRegisterManager().getActiveRegister().getContent();
        String text = StringUtils.multiply(registerContent.getText(), count);

        final String newLine = editorAdaptor.getConfiguration().getNewLine();
        text = VimUtils.replaceNewLines(text, newLine);

        ContentType pastingContentType = registerContent.getPayloadType();
        ContentType selectionContentType = editorAdaptor.getSelection().getContentType(editorAdaptor.getConfiguration());

        TextContent content = editorAdaptor.getModelContent();
        int offset = range.getLeftBound().getModelOffset();
        
        //use the default register for the DeleteOperation so the deleted text
        //will be stored in the default register, rather than overwriting the contents
        //of the current active register (which we're attempting to paste from)
        editorAdaptor.getRegisterManager().activateDefaultRegister();

        DeleteOperation.doIt(editorAdaptor, range, contentType);

        int position;
        if (selectionContentType == ContentType.LINES || pastingContentType == ContentType.LINES) {
            if (pastingContentType != ContentType.LINES) {
                text = text + newLine;
            } else if (selectionContentType != ContentType.LINES) {
                text = newLine + text;
            }
            content.replace(offset, 0, text);
            LineInformation firstPastedLine = content.getLineInformationOfOffset(offset + 1);
            position = VimUtils.getFirstNonWhiteSpaceOffset(content, firstPastedLine);
        } else {
            position = offset + text.length();
            // TextRange includes the last character. Move cursor back if going to normal mode.
            if (fixNormalModeCursor) {
                position -= 1;
            }
            content.replace(offset, 0, text);
        }
        // content.replace(offset, 0, StringUtils.multiply(text, count));
        Position destination = editorAdaptor.getCursorService().newPositionForModelOffset(position);
        editorAdaptor.setPosition(destination, StickyColumnPolicy.ON_CHANGE);
    }
}
