package newpackage.eclipse;

import newpackage.position.Position;
import newpackage.position.StartEndTextRange;

import org.eclipse.jface.text.ITextViewer;

public class StickyColumnPosition implements Position {

	private final ITextViewer textViewer;
	private final Position lineBegin;
	private final int stickyColumn;

	public StickyColumnPosition(ITextViewer textViewer, Position lineBegin, int stickyColumn) {
		this.textViewer = textViewer;
		this.lineBegin = lineBegin;
		this.stickyColumn = stickyColumn;
	}

	@Override
	public Position addModelOffset(int offset) {
		return new StickyColumnPosition(textViewer, lineBegin, getModelOffset() + offset);
	}

	@Override
	public Position addViewOffset(int offset) {
		return new StickyColumnPosition(textViewer, lineBegin, getViewOffset() + offset);
	}

	@Override
	public int getModelOffset() {
		return new StartEndTextRange(lineBegin, lineBegin.addViewOffset(stickyColumn)).getModelLength();
	}

	@Override
	public int getViewOffset() {
		return stickyColumn;
	}

}
