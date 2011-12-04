package net.sourceforge.vrapper.vim.commands;


public class Utils {

	public static int characterType(char chr) {
		if (Character.isWhitespace(chr))
			return 0;
		else if (chr == '_' || Character.isLetterOrDigit(chr))
			return 1;
		else
			return 2;
	}

}