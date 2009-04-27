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
	private CaretType caretType;

	public Position getPosition() {
	    if (selection != null) {
	        return selection.getEnd();
	    }
		return position;
	}

	public void setPosition(Position position, boolean updateColumn) {
	    this.selection = null;
		this.position = position;
	}

	public Position newPositionForModelOffset(int offset) {
		return new DumbPosition(offset);
	}

	public Position newPositionForViewOffset(int offset) {
		return new DumbPosition(offset);
	}

	public void setCaret(CaretType caretType) {
		this.caretType = caretType;
	}

	public CaretType getCaret() {
		return caretType;
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
