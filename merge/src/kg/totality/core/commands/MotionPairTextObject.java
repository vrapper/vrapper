package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.motions.Motion;
import kg.totality.core.utils.ContentType;
import newpackage.position.StartEndTextRange;
import newpackage.position.Position;
import newpackage.position.TextRange;

public class MotionPairTextObject extends AbstractTextObject {

	private final Motion toBeginning;
	private final Motion toEnd;

	public MotionPairTextObject(Motion toBeginning, Motion toEnd) {
		this.toBeginning = toBeginning;
		this.toEnd = toEnd;
	}

	@Override
	public TextRange getRegion(EditorAdaptor editorMode) {
		Position from = toBeginning.destination(editorMode);
		Position to = toEnd.destination(editorMode);
		return new StartEndTextRange(from, to);
	}

	@Override
	public TextRange getRegion(EditorAdaptor editorMode, int count) {
		Position from = toBeginning.destination(editorMode);
		Position to = toEnd.withCount(count).destination(editorMode);
		return new StartEndTextRange(from, to);
	}

	@Override
	public ContentType getContentType() {
		return ContentType.TEXT;
	}

}
