package net.sourceforge.vrapper.plugin.surround.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class FullLineTextObject implements TextObject {

	public TextRange getRegion(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
		TextContent model = editorAdaptor.getModelContent();
		LineInformation line = model.getLineInformationOfOffset(
				editorAdaptor.getCursorService().getPosition().getModelOffset()
				);
        int offset = VimUtils.getFirstNonWhiteSpaceOffset(model, line);
        Position left = editorAdaptor.getCursorService().newPositionForModelOffset(offset);
        Position right = editorAdaptor.getCursorService().newPositionForModelOffset(line.getEndOffset());
        
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
