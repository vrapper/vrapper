package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.register.RegisterContent;

public class PasteBeforeCommand extends CountAwareCommand {

    public static final PasteBeforeCommand CURSOR_ON_TEXT = new PasteBeforeCommand(false);
    public static final PasteBeforeCommand CURSOR_AFTER_TEXT = new PasteBeforeCommand(true);

    private boolean placeCursorAfter;
    
    private PasteBeforeCommand(boolean placeCursorAfter) {
    	this.placeCursorAfter = placeCursorAfter;
    }

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count) {
		if (!editorAdaptor.getFileService().isEditable()) {
            return;
        }
		if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
		RegisterContent registerContent = editorAdaptor.getRegisterManager().getActiveRegister().getContent();
		String text = registerContent.getText();
		TextContent content = editorAdaptor.getModelContent();
		int offset = editorAdaptor.getPosition().getModelOffset();
		boolean linewise = registerContent.getPayloadType() == ContentType.LINES;
        LineInformation line = content.getLineInformationOfOffset(offset);
		if (linewise) {
			offset = line.getBeginOffset();
        }
		try {
			editorAdaptor.getHistory().beginCompoundChange();
			content.replace(offset, 0, StringUtils.multiply(text, count));
			if (text.length() > 0) {
				int position;
				if (linewise)
		            position = placeCursorAfter && line.getNumber() + count < content.getNumberOfLines()
		            			? content.getLineInformation(line.getNumber() + count).getBeginOffset()
		            			: offset;
		            	
				else {
		        	position = offset;
					position += text.length();
					if (!placeCursorAfter)
						position -= 1;
				}
				Position destination = editorAdaptor.getCursorService().newPositionForModelOffset(position);
				editorAdaptor.setPosition(destination, true);
			}
		} finally {
			editorAdaptor.getHistory().endCompoundChange();
		}
	}

	@Override
	public CountAwareCommand repetition() {
		return this;
	}

}
