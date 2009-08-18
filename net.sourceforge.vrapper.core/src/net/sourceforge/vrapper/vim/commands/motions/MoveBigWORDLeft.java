package net.sourceforge.vrapper.vim.commands.motions;

public class MoveBigWORDLeft extends MoveWordLeft {

    public static final MoveBigWORDLeft INSTANCE = new MoveBigWORDLeft();

    private MoveBigWORDLeft() { /* NOP */ }

	@Override
	protected boolean atBoundary(char c1, char c2) {
		if (c1 == '\n' && c2 == '\n') {
            return true;
        }
		return Character.isWhitespace(c1) && !Character.isWhitespace(c2);
	}

}
