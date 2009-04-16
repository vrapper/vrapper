package kg.totality.core.commands.motions;

import kg.totality.core.EditorAdaptor;
import newpackage.glue.TextContent;
import newpackage.position.Position;




public abstract class MoveWithBounds extends CountAwareMotion {
	protected static final int BUFFER_LEN = 32;
	protected abstract boolean atBoundary(char c1, char c2);
	protected abstract boolean stopsAtNewlines();
	protected abstract boolean shouldStopAtLeftBoundingChar();
	protected abstract int destination(int offset, TextContent viewContent);

	@Override
	public boolean updateStickyColumn() {
		return true;
	}

	@Override
	public Position destination(EditorAdaptor editorAdaptor, int count) {
		if (count == NO_COUNT_GIVEN) count = 1;
		int offset = editorAdaptor.getPosition().getModelOffset();
		for (int i = 0; i < count; i++)
			offset = destination(offset, editorAdaptor.getModelContent());
		return editorAdaptor.getCursorService().newPositionForModelOffset(offset);
	}
}