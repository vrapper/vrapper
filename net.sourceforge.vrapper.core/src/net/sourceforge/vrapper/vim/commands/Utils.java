package net.sourceforge.vrapper.vim.commands;

import java.util.regex.Pattern;


public class Utils {
    
    public static final int WHITESPACE = 0;
    public static final int WORD = 1;
    public static final int OTHER = 2;

	public static int characterType(char chr, String iskeyword) {
		if (Character.isWhitespace(chr))
			return WHITESPACE;
		else if (Pattern.matches("["+iskeyword+"]", ""+chr))
			return WORD;
		else
			return OTHER;
	}

}