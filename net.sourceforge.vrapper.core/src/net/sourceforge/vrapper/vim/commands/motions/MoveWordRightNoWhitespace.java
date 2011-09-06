package net.sourceforge.vrapper.vim.commands.motions;

import static net.sourceforge.vrapper.vim.commands.Utils.characterType;

public class MoveWordRightNoWhitespace extends MoveWordRight {
	
	public static final MoveWordRightNoWhitespace INSTANCE = new MoveWordRightNoWhitespace(false);
	
    protected MoveWordRightNoWhitespace(boolean bailOff) {
        super(bailOff);
    }
    
    @Override
    protected boolean atBoundary(char c1, char c2) {
        return characterType(c1) != characterType(c2);
    } 

}
