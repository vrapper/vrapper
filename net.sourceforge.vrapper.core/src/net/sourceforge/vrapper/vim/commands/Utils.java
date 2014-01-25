package net.sourceforge.vrapper.vim.commands;

import java.util.regex.Pattern;


public class Utils {

	public static int characterType(char chr, String iskeyword) {
		if (Character.isWhitespace(chr))
			return 0;
		else if (Pattern.matches("["+iskeyword+"]", ""+chr))
			return 1;
		else
			return 2;
	}

}