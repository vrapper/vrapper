package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public interface Command extends Repeatable<Command>, Counted<Command> {
	/**
	 * Executes this command. This may have some side effects.
	 * @param editorAdaptor adaptor of editor this command was executed on
	 */
	public void execute(EditorAdaptor editorAdaptor);
}
