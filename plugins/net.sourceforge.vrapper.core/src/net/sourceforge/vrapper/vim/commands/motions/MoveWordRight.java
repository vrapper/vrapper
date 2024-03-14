package net.sourceforge.vrapper.vim.commands.motions;

import static net.sourceforge.vrapper.vim.commands.Utils.characterType;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;

public class MoveWordRight extends MoveRightWithBounds {

    public static final MoveWordRight INSTANCE = new MoveWordRight(false);
    public static final MoveWordRight BAILS_OFF = new MoveWordRight(true);
    
    protected MoveWordRight(boolean bailOff) {
        super(bailOff);
    }

    @Override
    protected boolean atBoundary(char c1, char c2) {
        return !Character.isWhitespace(c2) && characterType(c1, keywords) != characterType(c2, keywords);
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
