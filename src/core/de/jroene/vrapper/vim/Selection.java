package de.jroene.vrapper.vim;

import de.jroene.vrapper.vim.token.AbstractMove;
import de.jroene.vrapper.vim.token.Token;

public class Selection extends AbstractMove {

    private final int start;
    private final int length;

    public Selection(int start, int length) {
        super();
        this.start = start;
        this.length = length;
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

    public static Selection fromOffsets(int i1, int i2) {
        if ( i1 > i2) {
            int temp = i1;
            i1 = i2;
            i2 = temp;
        }
        return new Selection(i1, i2-i1);
    }

    @Override
    protected int calculateTarget(VimEmulator vim, Token next) {
        vim.getPlatform().setPosition(start);
        return getEnd();
    }

    @Override
    public boolean isHorizontal() {
        return true;
    }

    @Override
    public boolean includesTarget() {
        return true;
    }

}
