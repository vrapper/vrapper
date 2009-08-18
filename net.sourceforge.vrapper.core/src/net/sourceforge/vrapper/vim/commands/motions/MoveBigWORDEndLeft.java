package net.sourceforge.vrapper.vim.commands.motions;

public class MoveBigWORDEndLeft extends MoveWordEndLeft {

    public static final Motion INSTANCE = new MoveBigWORDEndLeft();

    private MoveBigWORDEndLeft() { /* NOP */ }

	@Override
	protected boolean atBoundary(char c1, char c2) {
		return !Character.isWhitespace(c1) && Character.isWhitespace(c2);
	}

}
