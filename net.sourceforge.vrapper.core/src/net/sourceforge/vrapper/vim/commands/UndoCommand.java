package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class UndoCommand extends SimpleRepeatableCommand {

	@Override
	public void execute(EditorAdaptor editorAdaptor) {
		editorAdaptor.getHistory().undo();
	}

	@Override
	public CountAwareCommand repetition() {
		return null;
	}

}
