package kg.totality.core.commands;

import newpackage.glue.CursorService;
import newpackage.glue.TextContent;
import newpackage.position.Position;
import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.motions.LineEndMotion;

public class StickToEOLCommand extends CountAwareCommand {

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count) {
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
