package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
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
		if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
		RegisterContent registerContent = editorAdaptor.getRegisterManager().getActiveRegister().getContent();
		if (registerContent.getPayloadType() == ContentType.TEXT_RECTANGLE) {
		    BlockPasteHelper.execute(editorAdaptor, count, 0, placeCursorAfter);
		    return;
		}
		String text = registerContent.getText();
		text = VimUtils.replaceNewLines(text, editorAdaptor.getConfiguration().getNewLine());
		TextContent content = editorAdaptor.getModelContent();
		boolean linewise = registerContent.getPayloadType() == ContentType.LINES;
		int offset = editorAdaptor.getPosition().getModelOffset();
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
					position += text.length() * count;
					if (!placeCursorAfter)
						position -= 1;
				}
				Position destination = editorAdaptor.getCursorService().newPositionForModelOffset(position);
				editorAdaptor.setPosition(destination, StickyColumnPolicy.ON_CHANGE);
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
