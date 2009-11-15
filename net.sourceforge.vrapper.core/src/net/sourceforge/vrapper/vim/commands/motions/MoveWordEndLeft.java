package net.sourceforge.vrapper.vim.commands.motions;

import static net.sourceforge.vrapper.vim.commands.Utils.characterType;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;

public class MoveWordEndLeft extends MoveLeftWithBounds {

    public MoveWordEndLeft(boolean bailOff) {
        super(bailOff);
    }

    public static final MoveWordEndLeft INSTANCE = new MoveWordEndLeft(false);
    public static final MoveWordEndLeft BAILS_OFF = new MoveWordEndLeft(true);

    @Override
    protected boolean atBoundary(char c1, char c2) {
        return !Character.isWhitespace(c1) && characterType(c1) != characterType(c2);
    }

    public BorderPolicy borderPolicy() {
        return BorderPolicy.INCLUSIVE;
    }

    @Override
    protected boolean shouldStopAtLeftBoundingChar() {
        return true;
    }

    @Override
    protected boolean stopsAtNewlines() {
        return true;
    }

}
