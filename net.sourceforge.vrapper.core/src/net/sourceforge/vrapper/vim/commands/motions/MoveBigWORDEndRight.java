package net.sourceforge.vrapper.vim.commands.motions;


public class MoveBigWORDEndRight extends MoveWordEndRight {

    protected MoveBigWORDEndRight(boolean bailOff) {
        super(bailOff);
    }

    public static final MoveBigWORDEndRight INSTANCE = new MoveBigWORDEndRight(false);
    public static final MoveBigWORDEndRight BAILS_OFF = new MoveBigWORDEndRight(true);

    @Override
	protected boolean atBoundary(char c1, char c2) {
		return !Character.isWhitespace(c1) && Character.isWhitespace(c2);
	}

}
