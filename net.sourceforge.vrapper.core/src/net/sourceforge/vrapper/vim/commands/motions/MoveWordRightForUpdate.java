package net.sourceforge.vrapper.vim.commands.motions;


public class MoveWordRightForUpdate extends MoveWordRight {

	public static final MoveWordRightForUpdate INSTANCE = new MoveWordRightForUpdate(false);
	
	protected MoveWordRightForUpdate(boolean bailOff) {
	    super(bailOff);
	}
	
	@Override
	public boolean trimsNewLinesFromEnd() {
	    return true;
	}
}