package kg.totality.core.commands.motions;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.BorderPolicy;
import newpackage.glue.TextContent;
import newpackage.position.Position;

public abstract class UpDownMotion extends CountAwareMotion {

	@Override
	public Position destination(EditorAdaptor editorAdaptor, int count) {
		if (count == NO_COUNT_GIVEN) count = 1;
		TextContent content = editorAdaptor.getViewContent();
		int oldOffset = editorAdaptor.getPosition().getViewOffset();

		int lineNo = content.getLineInformationOfOffset(oldOffset).getNumber() + getJump() * count;
        lineNo = Math.max(lineNo, 0);
        lineNo = Math.min(lineNo, content.getNumberOfLines()-1);
        return editorAdaptor.getCursorService().stickyColumnAtViewLine(lineNo);
	}

	protected abstract int getJump();

	@Override
	public BorderPolicy borderPolicy() {
		return BorderPolicy.LINE_WISE;
	}

	@Override
	public boolean updateStickyColumn() {
		return false;
	}

}