package net.sourceforge.vrapper.core.tests.utils;

import static java.lang.Math.min;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;

// TODO: currently caret can point behind the content
public class TestCursorAndSelection implements CursorService, SelectionService {

	private Position position = new DumbPosition(0);
	private Selection selection;
	private CaretType caretType;
    private TextContent content;
    private int stickyColumnNo;

	public Position getPosition() {
	    if (selection != null) {
	        return selection.getEnd();
	    }
		return position;
	}

	public void setPosition(Position position, boolean updateColumn) {
	    this.selection = null;
		this.position = position;
		if (updateColumn) {
		    int offset = position.getModelOffset();
            int beginOffset = content.getLineInformationOfOffset(offset).getBeginOffset();
            stickyColumnNo = offset - beginOffset;
        }
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
	    stickyColumnNo = Integer.MAX_VALUE;
	}

    @Override
    public void stickToBOL() {
        int offset = position.getModelOffset();
        int beginOffset = content.getLineInformationOfOffset(offset).getBeginOffset();
        stickyColumnNo = offset - beginOffset;
    }

	public Position stickyColumnAtModelLine(int lineNo) {
	    return stickyColumnAtViewLine(lineNo);
	}

	public Position stickyColumnAtViewLine(int lineNo) {
	    LineInformation lineInformation = content.getLineInformation(lineNo);
        int offset = lineInformation.getBeginOffset() + min(lineInformation.getLength(), stickyColumnNo);
        return new DumbPosition(offset);
	}

	public Selection getSelection() {
	    if (selection != null)
	        return selection;
	    else
	        //basically an empty selection centered around the cursor.
	        return new SimpleSelection(new StartEndTextRange(position, position));
	}

	public void setSelection(Selection selection) {
		if (selection != null)
			this.position = selection.getEnd();
		this.selection = selection;
	}

	public void setLineWiseSelection(boolean lineWise) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not yet implemented");
	}

    public void setContent(TextContent content) {
        this.content = content;
    }

    public Position getMark(String id) {
        return null;
    }

    public void setMark(String id, Position position) {
        // do nothing
    }

	public Position getNextChangeLocation(int count) {
		// TODO Auto-generated method stub
		return null;
	}

	public Position getPrevChangeLocation(int count) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public int getVisualOffset(Position position) {
	    LineInformation lineInformation = content.getLineInformationOfOffset(position.getModelOffset());
        return position.getModelOffset() - lineInformation.getBeginOffset();
    }

    @Override
    public Position getPositionByVisualOffset(int lineNo, int visualOffset) {
	    LineInformation lineInformation = content.getLineInformation(lineNo);
        return new DumbPosition(lineInformation.getBeginOffset() + visualOffset);
    }

    @Override
    public int visualWidthToChars(int visualWidth) {
        return visualWidth;
    }
}