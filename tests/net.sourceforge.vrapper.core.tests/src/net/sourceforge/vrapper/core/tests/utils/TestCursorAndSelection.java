package net.sourceforge.vrapper.core.tests.utils;

import static java.lang.Math.min;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

// TODO: currently caret can point behind the content
public class TestCursorAndSelection implements CursorService, SelectionService {

	private Position position = new DumbPosition(0);
	private Selection selection;
	private TextRange nativeSelection;
	private CaretType caretType;
    private TextContent content;
    private int stickyColumnNo;

	public Position getPosition() {
		if (selection != null) {
			return selection.getTo();
		}
		return position;
	}

    @Override
    public void setPosition(Position position, StickyColumnPolicy columnPolicy) {
        this.selection = null;
        this.position = position;
        if (columnPolicy == StickyColumnPolicy.TO_EOL) {
            stickyColumnNo = Integer.MAX_VALUE;
        } else if (columnPolicy != StickyColumnPolicy.NEVER) {
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

	@Override
	public Position newPositionForModelOffset(int targetModelOffset, Position original,
				boolean allowPastLastChar) {
		int modelOffset = original.getModelOffset();
		int delta = targetModelOffset - modelOffset;
		return shiftPositionForModelOffset(modelOffset, delta, allowPastLastChar);
	}

	@Override
	public Position shiftPositionForModelOffset(int offset, int delta, boolean allowPastLastChar) {
		int targetOffset = offset + delta;
		if (delta == 0) {
			return new DumbPosition(offset);
		} else if (targetOffset <= 0) {
			return new DumbPosition(0);
		} else if (targetOffset > content.getTextLength()) {
			// Clip to text end, but 'onNewline' still might need corrections
			targetOffset = content.getTextLength();
		}
		LineInformation lineInfo = content.getLineInformationOfOffset(targetOffset);
		if (targetOffset == content.getTextLength()) {
			// Fall through to 'onNewline' check.
		} else if (delta > 0 && targetOffset > lineInfo.getEndOffset()) {
			// Moving to right but we fall just outside the line. Skip to beginning of next line.
			lineInfo = content.getLineInformation(lineInfo.getNumber() + 1);
			targetOffset = lineInfo.getBeginOffset();
		} else if (delta < 0 && targetOffset > lineInfo.getEndOffset()) {
			// Moving to right but we fall just outside the line. Skip to end of line.
			targetOffset = lineInfo.getEndOffset();
		}
		if ( ! allowPastLastChar && targetOffset == lineInfo.getEndOffset()
				&& targetOffset > lineInfo.getBeginOffset()) {
			// Past last character and the line isn't empty. Move one back.
			targetOffset--;
		}
		return new DumbPosition(targetOffset);
	}

	@Override
	public Position shiftPositionForViewOffset(int offset, int delta, boolean allowPastLastChar) {
		// Tests have no difference between view and model space.
		return shiftPositionForModelOffset(offset, delta, allowPastLastChar);
	}

    public void setCaret(CaretType caretType) {
		this.caretType = caretType;
	}

	public CaretType getCaret() {
		return caretType;
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
		if (selection != null) {
			return selection;
		} else if (nativeSelection != null) {
			return new SimpleSelection(nativeSelection);
		} else {
			//basically an empty selection centered around the cursor.
			return new SimpleSelection(new StartEndTextRange(position, position));
		}
	}
	
	@Override
	public TextRange getNativeSelection() {
		if (selection == null && nativeSelection == null) {
			//basically an empty selection centered around the cursor.
			return new StartEndTextRange(position, position);
		} else if (selection == null) {
			return nativeSelection;
		} else {
			// regular Vrapper selection always has priority over native selection
			return VRAPPER_SELECTION_ACTIVE;
		}
	}
	
	@Override
	public void setNativeSelection(TextRange range) {
		selection = null;
		nativeSelection = range;
	}

	public void setSelection(Selection selection) {
		if (selection != null)
			this.position = selection.getEnd();
		this.selection = selection;
		if (selection == null) {
			nativeSelection = null;
		}
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

    public void deleteMark(String id) {
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

	@Override
	public Set<String> getAllMarks() {
		return new HashSet<String>();
	}

	@Override
	public boolean isGlobalMark(String id) {
		return false;
	}

    @Override
    public boolean shouldStickToEOL() {
        return stickyColumnNo == Integer.MAX_VALUE;
    }

    @Override
    public void markCurrentPosition() {}

    @Override
    public void updateLastPosition() {}
}
