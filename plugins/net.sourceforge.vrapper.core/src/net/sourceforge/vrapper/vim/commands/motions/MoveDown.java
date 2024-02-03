package net.sourceforge.vrapper.vim.commands.motions;


public class MoveDown extends UpDownMotion {

    public static final Motion INSTANCE = new MoveDown();

    private MoveDown() { /* NOP */ }

	@Override protected int getJump() { return 1; }
}
