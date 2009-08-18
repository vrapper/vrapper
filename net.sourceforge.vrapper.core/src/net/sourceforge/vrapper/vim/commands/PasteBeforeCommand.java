package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.register.RegisterContent;

public class PasteBeforeCommand extends CountAwareCommand {

    public static final PasteBeforeCommand INSTANCE = new PasteBeforeCommand();

    private PasteBeforeCommand() { /* NOP */ }

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
		if (registerContent.getPayloadType() == ContentType.LINES) {
            offset = content.getLineInformationOfOffset(offset).getBeginOffset();
        }
		int position = offset;
		try {
			editorAdaptor.getHistory().beginCompoundChange();
			content.replace(offset, 0, StringUtils.multiply(text, count));
			// TODO: compatibility option: position vs. offset for destination
			Position destination = editorAdaptor.getCursorService().newPositionForModelOffset(position);
			editorAdaptor.setPosition(destination, true);
		} finally {
			editorAdaptor.getHistory().endCompoundChange();
		}
	}

	@Override
	public CountAwareCommand repetition() {
		return this;
	}

}
