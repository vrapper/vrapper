package net.sourceforge.vrapper.vim.commands.motions;

import static net.sourceforge.vrapper.vim.commands.Utils.characterType;

public class MoveWordEndRightForChange extends MoveWordEndRight {
	
    public static final MoveWordEndRightForChange INSTANCE = new MoveWordEndRightForChange(true);
	
    protected MoveWordEndRightForChange(boolean bailOff) {
        super(bailOff);
    }

    @Override
    protected boolean atBoundary(char c1, char c2) {
        return !Character.isWhitespace(c1) && characterType(c1) != characterType(c2)
        || Character.isWhitespace(c1) && characterType(c1) != characterType(c2);
    }

}
