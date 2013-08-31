package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterManager;

public class PasteRegisterCommand extends CountIgnoringNonRepeatableCommand {
	
	public static final PasteRegisterCommand PASTE_LAST_INSERT =
		new PasteRegisterCommand(RegisterManager.REGISTER_NAME_INSERT);

	private String registerName;

	public PasteRegisterCommand(String registerName) {
		this.registerName = registerName;
	}

	public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
		Register reg = editorAdaptor.getRegisterManager().getRegister(registerName);
		String text = reg.getContent().getText();
		text = VimUtils.replaceNewLines(text, editorAdaptor.getConfiguration().getNewLine());

		if(text.length() > 0) {
			TextContent content = editorAdaptor.getModelContent();
			//Delete Eclipse selection contents, for example when completing a function's arguments.
			Selection currentSelection = editorAdaptor.getSelection();
			if (currentSelection.getModelLength() > 0) {
				content.replace(currentSelection.getLeftBound().getModelOffset(),
						currentSelection.getModelLength(), "");
			}
			int offset = editorAdaptor.getCursorService().getPosition().getModelOffset();
			//different from PasteOperation! it does length() - 1
			int position = offset + text.length();
			content.replace(offset, 0, text);
			Position destination = editorAdaptor.getCursorService().newPositionForModelOffset(position);
			editorAdaptor.setPosition(destination, StickyColumnPolicy.ON_CHANGE);
		}
	}

}
