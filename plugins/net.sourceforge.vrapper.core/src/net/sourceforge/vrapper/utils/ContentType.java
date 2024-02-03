package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.vim.commands.BorderPolicy;

public enum ContentType {
	TEXT, LINES, TEXT_RECTANGLE, KEY_SEQUENCE;
	public static ContentType fromBorderPolicy(BorderPolicy policy) {
		switch (policy) {
		case LINE_WISE: return LINES;
		case RECTANGLE: return TEXT_RECTANGLE;
		default:
			return TEXT;
		}
	}
}
