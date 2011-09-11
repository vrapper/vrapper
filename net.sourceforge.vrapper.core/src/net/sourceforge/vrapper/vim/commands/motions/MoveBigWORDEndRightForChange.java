package net.sourceforge.vrapper.vim.commands.motions;

public class MoveBigWORDEndRightForChange extends MoveBigWORDEndRight {

    public static final MoveBigWORDEndRightForChange INSTANCE = new MoveBigWORDEndRightForChange(true);
    
    protected MoveBigWORDEndRightForChange(boolean bailOff) {
        super(bailOff);
    }

    @Override
	protected boolean atBoundary(char c1, char c2) {
		return !Character.isWhitespace(c1) && Character.isWhitespace(c2)
        || Character.isWhitespace(c1) && ! Character.isWhitespace(c2);
	}
}
