package net.sourceforge.vrapper.vim.commands.motions;


public class MoveDown extends UpDownMotion {
	@Override protected int getJump() { return 1; }
}
