package kg.totality.core.commands.motions;

import kg.totality.core.EditorAdaptor;
import newpackage.glue.TextContent;
import newpackage.position.Position;

public abstract class AbstractModelSideMotion extends CountAwareMotion {

	protected abstract int destination(int offset, TextContent content, int count);

	@Override
	public Position destination(EditorAdaptor editorAdaptor, int count) {
		if (count == NO_COUNT_GIVEN) count = 1;
		int modelOffset = editorAdaptor.getCursorService().getPosition().getModelOffset();
		TextContent modelContent = editorAdaptor.getModelContent();
		int destination = destination(modelOffset, modelContent, count);
		return editorAdaptor.getCursorService().newPositionForModelOffset(destination);
	}

	@Override
	public boolean updateStickyColumn() {
		return true;
	}

}
