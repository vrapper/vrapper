package net.sourceforge.vrapper.core.tests.utils;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;

// TODO: currently caret can point behind the content
public class TestCursorAndSelection implements CursorService, SelectionService {

	private Position position = new DumbPosition(0);
	private TextRange selection;

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position, boolean updateColumn) {
		this.position = position;
		// TODO: sticky column
	}

	public Position newPositionForModelOffset(int offset) {
		return new DumbPosition(offset);
	}

	public Position newPositionForViewOffset(int offset) {
		return new DumbPosition(offset);
	}

	public void setCaret(CaretType caretType) {
		// TODO Auto-generated method stub
	}

	public void stickToEOL() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not yet implemented");
	}

	public Position stickyColumnAtModelLine(int lineNo) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not yet implemented");
	}

	public Position stickyColumnAtViewLine(int lineNo) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not yet implemented");
	}

	public TextRange getSelection() {
		return selection;
	}

	public void setSelection(TextRange selection) {
		this.selection = selection;
	}

	public void setLineWiseSelection(boolean lineWise) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not yet implemented");
	}

}
