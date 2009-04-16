package net.sourceforge.vrapper.utils;

public class StartEndTextRange implements TextRange {

	private final Position start;
	private final Position end;

	public StartEndTextRange(Position start, Position end) {
		this.start = start;
		this.end = end;
	}

	@Override
	public Position getStart() {
		return start;
	}

	@Override
	public Position getEnd() {
		return end;
	}

	@Override
	public Position getLeftBound() {
		return !isReversed() ? getStart() : getEnd();
	}

	@Override
	public Position getRightBound() {
		return !isReversed() ? getEnd() : getStart();
	}

	@Override
	public int getModelLength() {
		return Math.abs(getSignedModelLength());
	}

	@Override
	public int getViewLength() {
		return Math.abs(getEnd().getViewOffset() - getStart().getViewOffset());
	}

	@Override
	public boolean isReversed() {
		return getSignedModelLength() < 0;
	}

	private int getSignedModelLength() {
		return getEnd().getModelOffset() - getStart().getModelOffset();
	}

}
