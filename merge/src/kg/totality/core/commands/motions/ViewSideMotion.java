package kg.totality.core.commands.motions;

import kg.totality.core.EditorAdaptor;
import newpackage.glue.TextContent;
import newpackage.position.Position;

public abstract class ViewSideMotion implements Motion {

	@Override
	public Position destination(EditorAdaptor editorAdaptor) {
		TextContent content = editorAdaptor.getViewContent();
		Position position = editorAdaptor.getPosition();
		int offset = position.getViewOffset();
		return position.addViewOffset(getJump(offset, content));
	}

	protected abstract int getJump(int offset, TextContent content);

}
