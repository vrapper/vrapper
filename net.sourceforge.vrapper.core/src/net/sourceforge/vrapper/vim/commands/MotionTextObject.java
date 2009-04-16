package net.sourceforge.vrapper.vim.commands;

import static java.lang.Math.min;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class MotionTextObject extends AbstractTextObject {

	private Motion motion;

	public MotionTextObject(Motion move) {
		this.motion = move;
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
	public ContentType getContentType() {
		return ContentType.fromBorderPolicy(motion.borderPolicy());
	}

}
