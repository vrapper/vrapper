package net.sourceforge.vrapper.vim.commands;

public enum BorderPolicy {
	/** Unused, likely has undefined behavior. */
	DONT_CARE,
	/** If cursor is on a char, use the offset of the cursor for a selection. */
	EXCLUSIVE,
	/**
	 * If cursor is on a char, use the offset of the character to the right of the cursor for a
	 *  selection (i.e. include the character we are on. )
	 */
	INCLUSIVE,
	/** Include the entire line the cursor is on. */
	LINE_WISE,
	/** Currently unused, no motions select in block. */
	RECTANGLE // TODO: implement ;-)
}
