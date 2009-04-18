package net.sourceforge.vrapper.utils;

public class StartEndTextRange implements TextRange {

    private final Position start;
    private final Position end;

    public StartEndTextRange(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    public Position getStart() {
        return start;
    }

    public Position getEnd() {
        return end;
    }

    public Position getLeftBound() {
        return !isReversed() ? getStart() : getEnd();
    }

    public Position getRightBound() {
        return !isReversed() ? getEnd() : getStart();
    }

    public int getModelLength() {
        return Math.abs(getSignedModelLength());
    }

    public int getViewLength() {
        return Math.abs(getEnd().getViewOffset() - getStart().getViewOffset());
    }

    public boolean isReversed() {
        return getSignedModelLength() < 0;
    }

    private int getSignedModelLength() {
        return getEnd().getModelOffset() - getStart().getModelOffset();
    }

}
