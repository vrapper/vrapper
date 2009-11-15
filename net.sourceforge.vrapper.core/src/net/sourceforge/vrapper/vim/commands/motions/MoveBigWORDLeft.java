package net.sourceforge.vrapper.vim.commands.motions;

public class MoveBigWORDLeft extends MoveWordLeft {

    protected MoveBigWORDLeft(boolean bailOff) {
        super(bailOff);
    }

    public static final MoveBigWORDLeft INSTANCE = new MoveBigWORDLeft(false);
    public static final MoveBigWORDLeft BAILS_OFF = new MoveBigWORDLeft(true);

	@Override
	protected boolean atBoundary(char c1, char c2) {
		if (c1 == '\n' && c2 == '\n') {
            return true;
        }
		return Character.isWhitespace(c1) && !Character.isWhitespace(c2);
	}

}
