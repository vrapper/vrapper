package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Special text object matching the current line from the first non-whitespace charactor to the
 * last non-whitespace character.
 */
public class InnerLineTextObject implements TextObject {

    public TextRange getRegion(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        TextContent model = editorAdaptor.getModelContent();
        int modelOffset = editorAdaptor.getCursorService().getPosition().getModelOffset();
        LineInformation line = model.getLineInformationOfOffset(modelOffset);
        int startOffset = VimUtils.getFirstNonWhiteSpaceOffset(model, line);
        int endOffset = VimUtils.getLastNonWhiteSpaceOffset(model, line);
        // Include last char, but not the newline.
        if (endOffset < line.getEndOffset()) {
            endOffset += 1;
        }
        Position left = editorAdaptor.getCursorService().newPositionForModelOffset(startOffset);
        Position right = editorAdaptor.getCursorService().newPositionForModelOffset(endOffset);
        return new StartEndTextRange(left, right);
    }

    public ContentType getContentType(Configuration configuration) {
        return ContentType.TEXT;
    }

    public TextObject withCount(int count) {
        return null;
    }

    public int getCount() {
        return 0;
    }

}
