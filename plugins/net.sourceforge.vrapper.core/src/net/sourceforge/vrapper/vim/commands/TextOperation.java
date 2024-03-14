package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public interface TextOperation {

	void execute(EditorAdaptor editorAdaptor, int count, TextObject textObject) throws CommandExecutionException;

	TextOperation repetition();

}
