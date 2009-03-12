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

    public static Selection fromOffsets(int i1, int i2, boolean lineWise) {
        if ( i1 > i2) {
            int temp = i1;
            i1 = i2;
            i2 = temp;
        }
        return new Selection(i1, i2-i1, lineWise);
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
