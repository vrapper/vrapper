package newpackage.position;

/**
 * A range of text.
 * TextRanges are directed.
 * TextRange is reversed if it's start is on the right of it's end.
 *
 * Invariant:
 *   (isReversed() && getStart().equals(getRightBound()) && getEnd().equals(getLeftBound())) ||
 *  (!isReversed() && getStart().equals(getLeftBound())  && getEnd().equals(getRightBound()))
 *
 * @author Krzysiek Goj
 */
public interface TextRange {
	/** @return start of this text range. */
	Position getStart();
	/** @return end of this text range. */
	Position getEnd();

	/** @return length of this text range in model space */
	int getModelLength();
	/** @return length of this text range in view space */
	int getViewLength();

	/** @return is this text range reversed? */
	boolean isReversed();

	/** @return left bound of this text range. inclusive */
	Position getLeftBound();
	/** @return left bound of this text range. exclusive */
	Position getRightBound();
}
