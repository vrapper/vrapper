package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class RedoCommand extends SimpleRepeatableCommand {

	@Override
	public void execute(EditorAdaptor editorAdaptor) {
		editorAdaptor.getHistory().redo();
	}

	@Override
	public CountAwareCommand repetition() {
		return null;
	}

}
