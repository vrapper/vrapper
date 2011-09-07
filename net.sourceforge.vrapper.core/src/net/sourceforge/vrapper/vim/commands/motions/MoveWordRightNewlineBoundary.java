package net.sourceforge.vrapper.vim.commands.motions;

import static net.sourceforge.vrapper.vim.commands.Utils.characterType;
import static net.sourceforge.vrapper.vim.commands.Utils.isNewLineCharacter;

public class MoveWordRightNewlineBoundary extends MoveWordRight {
	
	public static final MoveWordRightNewlineBoundary INSTANCE = new MoveWordRightNewlineBoundary(false);
	
    protected MoveWordRightNewlineBoundary(boolean bailOff) {
        super(bailOff);
    }
    
    @Override
    protected boolean atBoundary(char c1, char c2) {
        return isNewLineCharacter(c2) || (!Character.isWhitespace(c2) && characterType(c1) != characterType(c2));
    } 

}
