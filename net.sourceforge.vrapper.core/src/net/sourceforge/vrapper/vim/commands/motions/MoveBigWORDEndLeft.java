package net.sourceforge.vrapper.vim.commands.motions;

public class MoveBigWORDEndLeft extends MoveWordEndLeft {

	@Override
	protected boolean atBoundary(char c1, char c2) {
		return !Character.isWhitespace(c1) && Character.isWhitespace(c2);
	}

}
