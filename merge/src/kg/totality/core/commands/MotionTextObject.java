package kg.totality.core.commands;

import static java.lang.Math.min;
import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.motions.Motion;
import kg.totality.core.utils.ContentType;
import newpackage.glue.CursorService;
import newpackage.glue.TextContent;
import newpackage.position.Position;
import newpackage.position.StartEndTextRange;
import newpackage.position.TextRange;

public class MotionTextObject extends AbstractTextObject {

	private Motion motion;

	public MotionTextObject(Motion move) {
		this.motion = move;
	}

	@Override
	public TextRange getRegion(EditorAdaptor editorMode) {
		Position from = editorMode.getPosition();
		Position to = motion.destination(editorMode);
		return applyBorderPolicy(editorMode, from, to);
	}

	@Override
	public TextRange getRegion(EditorAdaptor editorMode, int count) {
		Position from = editorMode.getPosition();
		Position to = motion.withCount(count).destination(editorMode);
		return applyBorderPolicy(editorMode, from, to);
	};

	private TextRange applyBorderPolicy(EditorAdaptor editorMode, Position from, Position to) {
		switch (motion.borderPolicy()) {
		case EXCLUSIVE: return new StartEndTextRange(from, to);
		case INCLUSIVE: return new StartEndTextRange(from, to.addModelOffset(1));
		case LINE_WISE: return lines(editorMode, from, to);
		default:
			throw new RuntimeException("unsupported border policy: " + motion.borderPolicy());
		}
	}

	private static TextRange lines(EditorAdaptor editorMode, Position from, Position to) {
		TextRange range = new StartEndTextRange(from, to);
		TextContent content = editorMode.getModelContent();
		int start = range.getLeftBound().getModelOffset();
		int end   = range.getRightBound().getModelOffset();
		start = content.getLineInformationOfOffset(start).getBeginOffset();
		end = content.getLineInformationOfOffset(end).getEndOffset() + 1;
		end = min(end, content.getTextLength());
		CursorService cs = editorMode.getCursorService();
		return new StartEndTextRange(cs.newPositionForModelOffset(start), cs.newPositionForModelOffset(end));
	}

	@Override
	public kg.totality.core.utils.ContentType getContentType() {
		return ContentType.fromBorderPolicy(motion.borderPolicy());
	}

}
