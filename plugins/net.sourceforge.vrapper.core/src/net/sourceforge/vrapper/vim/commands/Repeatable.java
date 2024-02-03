package net.sourceforge.vrapper.vim.commands;

public interface Repeatable<T> {
	/** returns T that's repetition (eg. Command repeated by {@link DotCommand}) of this one
	 * @return repetition or <code>null</code> if this command is not repeatable
	 */
	T repetition();
}
