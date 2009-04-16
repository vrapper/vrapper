package de.jroene.vrapper.vim;

import de.jroene.vrapper.vim.token.AbstractMove;
import de.jroene.vrapper.vim.token.Token;

public class Selection extends AbstractMove {

    private final int start;
    private final int length;
    private final boolean lineWise;

    public Selection(int start, int length) {
        this(start, length, false);
    }

    public Selection(int start, int length, boolean lineWise) {
        super();
        this.start = start;
        this.length = length;
        this.lineWise = lineWise;
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }

    public int getEnd() {
        return start+length;
    }

    public boolean isLineWise() {
        return lineWise;
    }

    public static Selection fromOffsets(int i1, int i2, boolean lineWise) {
        return new Selection(i1, i2-i1, lineWise);
    }

    public int getLeftSide() {
    	return Math.min(start, start+length);
    }

    public int getRightSide() {
    	return Math.max(start, start+length);
    }

    public boolean isReversed() {
    	return length < 0;
    }

    public Selection reverse() {
    	return new Selection(start, -length, lineWise);
    }

    @Override
    protected int calculateTarget(VimEmulator vim, Token next) {
        if (length == 0) {
            return -1;
        }
        vim.getPlatform().setPosition(start);
        return getEnd();
    }

    @Override
    public boolean isHorizontal() {
        return !lineWise;
    }

}
