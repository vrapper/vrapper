package net.sourceforge.vrapper.vim.commands.motions;


public class MoveUp extends UpDownMotion {
	@Override protected int getJump() { return -1; }
}
