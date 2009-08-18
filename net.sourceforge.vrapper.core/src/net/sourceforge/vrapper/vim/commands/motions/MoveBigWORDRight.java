package net.sourceforge.vrapper.vim.commands.motions;


public class MoveBigWORDRight extends MoveWordRight {

    public static final MoveBigWORDRight INSTANCE = new MoveBigWORDRight();

    private MoveBigWORDRight() { /* NOP */ }

	@Override
	protected boolean atBoundary(char c1, char c2) {
		if (c1 == '\n' && c2 == '\n') {
            return true;
        }
		return Character.isWhitespace(c1) && !Character.isWhitespace(c2);
	}
}
