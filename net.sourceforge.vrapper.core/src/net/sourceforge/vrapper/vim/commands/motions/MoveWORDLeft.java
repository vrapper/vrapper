package net.sourceforge.vrapper.vim.commands.motions;

public class MoveWORDLeft extends MoveWordLeft {

	@Override
	protected boolean atBoundary(char c1, char c2) {
		if (c1 == '\n' && c2 == '\n')
			return true;
		return Character.isWhitespace(c1) && !Character.isWhitespace(c2);
	}

}
