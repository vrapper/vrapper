package net.sourceforge.vrapper.vim.commands.motions;

public class MoveBigWORDEndLeft extends MoveWordEndLeft {

    private MoveBigWORDEndLeft(boolean bailOff) {
        super(bailOff);
    }

    public static final Motion INSTANCE = new MoveBigWORDEndLeft(false);
    public static final Motion BAILS_OFF = new MoveBigWORDEndLeft(true);

	@Override
	protected boolean atBoundary(char c1, char c2) {
		return !Character.isWhitespace(c1) && Character.isWhitespace(c2);
	}

}
