package net.sourceforge.vrapper.vim.commands.motions;

import static net.sourceforge.vrapper.vim.commands.Utils.characterType;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;

public class MoveWordRight extends MoveRightWithBounds {

    public static final MoveWordRight INSTANCE = new MoveWordRight();

    protected MoveWordRight() { /* NOP */ }

    @Override
    protected boolean atBoundary(char c1, char c2) {
        return !Character.isWhitespace(c2) && characterType(c1) != characterType(c2);
    }

    public BorderPolicy borderPolicy() {
        return BorderPolicy.EXCLUSIVE;
    }

    @Override
    protected boolean shouldStopAtLeftBoundingChar() {
        return false;
    }

    @Override
    protected boolean stopsAtNewlines() {
        return true;
    }

}
