package net.sourceforge.vrapper.vim.commands.motions;


public class MoveBigWORDRight extends MoveWordRight {

    protected MoveBigWORDRight(boolean bailOff) {
        super(bailOff);
    }

    public static final MoveBigWORDRight INSTANCE = new MoveBigWORDRight(false);
    public static final MoveBigWORDRight BAILS_OFF = new MoveBigWORDRight(true);

	@Override
	protected boolean atBoundary(char c1, char c2) {
		if (c1 == '\n' && c2 == '\n') {
            return true;
        }
		return Character.isWhitespace(c1) && !Character.isWhitespace(c2);
	}
}
