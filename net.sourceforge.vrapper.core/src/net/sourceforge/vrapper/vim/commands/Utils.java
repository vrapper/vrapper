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
	
	public static boolean isNewLineCharacter(char chr) {
		return (chr=='\n' || 
				chr=='\r' || 
				chr=='\f' || 
				chr=='\u2028' || 
				chr=='\u2029' ||
				chr=='\u0085' ||
				chr=='\u000C'
			);
	}

}