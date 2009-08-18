package net.sourceforge.vrapper.vim.commands.motions;


public class MoveBigWORDEndRight extends MoveWordEndRight {

    public static final MoveBigWORDEndRight INSTANCE = new MoveBigWORDEndRight();

    private MoveBigWORDEndRight() { /* NOP */ }

	@Override
	protected boolean atBoundary(char c1, char c2) {
		return !Character.isWhitespace(c1) && Character.isWhitespace(c2);
	}

}
