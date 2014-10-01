package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.register.RegisterContent;

public class PasteAfterCommand extends CountAwareCommand {

    public static final PasteAfterCommand CURSOR_ON_TEXT = new PasteAfterCommand(false);
    public static final PasteAfterCommand CURSOR_AFTER_TEXT = new PasteAfterCommand(true);

    private boolean placeCursorAfter;
    
    private PasteAfterCommand(boolean placeCursorAfter) {
    	this.placeCursorAfter = placeCursorAfter;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count) {
        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
        final CursorService cursorService = editorAdaptor.getCursorService();
        RegisterContent registerContent = editorAdaptor.getRegisterManager().getActiveRegister().getContent();
        String text = registerContent.getText();
        text = VimUtils.replaceNewLines(text, editorAdaptor.getConfiguration().getNewLine());
        TextContent content = editorAdaptor.getModelContent();
        int offset = editorAdaptor.getPosition().getModelOffset();
        LineInformation line = content.getLineInformationOfOffset(offset);
        int lineNo = line.getNumber() + 1;
        int position;
        switch (registerContent.getPayloadType()) {
        case LINES:
            // FIXME: position calculation for count > 1
            if (lineNo < content.getNumberOfLines()) {
                offset = content.getLineInformation(lineNo).getBeginOffset();
                position = offset;
            } else {
                offset = content.getTextLength();
                String newLine = editorAdaptor.getConfiguration().getNewLine();
                text = newLine + VimUtils.stripLastNewline(text);
                position = offset + newLine.length();
            }
            break;
        case TEXT:
            offset = Math.min(line.getEndOffset(), offset + 1);
            position = offset + text.length() * count;
            // Move cursor back, unless we should be after the pasted text or if line is empty.
            if ( ( ! placeCursorAfter || text.length() == 0) && line.getLength() > 0) {
                position -= 1;
            }
            break;
        case TEXT_RECTANGLE:
            BlockPasteHelper.execute(editorAdaptor, count, 1, placeCursorAfter);
            return;
        default:
            return;
        }
        try {
            editorAdaptor.getHistory().beginCompoundChange();
            String multipliedText = StringUtils.multiply(text, count);
            content.replace(offset, 0, multipliedText);
            int followingLine = lineNo + (StringUtils.countNewlines(text) * count);
            if (registerContent.getPayloadType() == ContentType.LINES && placeCursorAfter
				&& followingLine < content.getNumberOfLines()) {
	                position = content.getLineInformation(followingLine).getBeginOffset();
            }

            Position start = cursorService.getPosition().setModelOffset(offset);
            //if multipliedText ends with a windows newline (\r\n) step cursor back two characters
            //otherwise, step cursor back one character
            int tweak = Math.max(1, VimUtils.endsWithNewLine(multipliedText));
            Position end = start.addModelOffset(multipliedText.length() - tweak);
            cursorService.setMark(CursorService.LAST_CHANGE_START, start);
            cursorService.setMark(CursorService.LAST_CHANGE_END, end);

            Position destination = cursorService.newPositionForModelOffset(position);
            editorAdaptor.setPosition(destination, StickyColumnPolicy.ON_CHANGE);
        } finally {
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

    @Override
    public CountAwareCommand repetition() {
        return this;
    }


}
