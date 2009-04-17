package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.LineEndMotion;

public class StickToEOLCommand extends CountAwareCommand {

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count) {
		if (count == NO_COUNT_GIVEN) count = 1;
		CursorService cursorService = editorAdaptor.getCursorService();
		int offset = cursorService.getPosition().getModelOffset();
		TextContent content = editorAdaptor.getModelContent();
		int destination = LineEndMotion.getDestination(offset, content, count);
		Position position = cursorService.newPositionForModelOffset(destination);
		cursorService.stickToEOL();
		cursorService.setPosition(position, false);
	}

	@Override
	public CountAwareCommand repetition() {
		return null;
	}

}
