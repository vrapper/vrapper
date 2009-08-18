package net.sourceforge.vrapper.vim.commands.motions;


public class MoveUp extends UpDownMotion {

    public static final MoveUp INSTANCE = new MoveUp();

    private MoveUp() { /* NOP */ }

	@Override protected int getJump() { return -1; }
}
