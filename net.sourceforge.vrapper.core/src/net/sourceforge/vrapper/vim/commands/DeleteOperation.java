package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
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
            TextContent txtContent = editorAdaptor.getModelContent();
            CursorService cur = editorAdaptor.getCursorService();
            int position = range.getLeftBound().getModelOffset();
            int length = range.getModelLength();
            
            String text = txtContent.getText(position, length);
            //if we're in LINES mode but the text doesn't end in a newline
            //try to include the previous newline character
            //(this is mostly to handle the last line of a file)
            if(contentType == ContentType.LINES && position > 0
                    && (text.length() == 0 || ! Utils.isNewLineCharacter(text.charAt(text.length()-1)))) {
                //grab the previous newline
                LineInformation line = txtContent.getLineInformationOfOffset(position);
                int previousNewlinePos = txtContent.getLineInformation(line.getNumber() - 1).getEndOffset();
                length += position - previousNewlinePos;
                position = previousNewlinePos;
            }

            txtContent.replace(position, length, "");

            if (contentType == ContentType.LINES) {
                // move cursor on indented position
                // this is Vim-compatible, but does everyone really want this?
                // FIXME: make this an option
                LineInformation lastLine = txtContent.getLineInformationOfOffset(position);
                int indent = VimUtils.getIndent(txtContent, lastLine).length();
                int offset = lastLine.getBeginOffset() + indent;
                cur.setPosition(cur.newPositionForModelOffset(offset), true);
            } else // fix sticky column
                cur.setPosition(cur.getPosition(), true);
        }
    }
}
