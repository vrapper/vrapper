package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public interface Command extends Repeatable<Command>, Counted<Command> {
	/**
	 * Executes this command. This usually have some side effects on editorAdaptor.
	 * @param editorAdaptor adaptor of editor this command was executed on
	 * @throws CommandExecutionException
	 */
	public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException;
}
