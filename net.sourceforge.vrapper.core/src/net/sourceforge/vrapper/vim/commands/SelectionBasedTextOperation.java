package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SelectionBasedTextOperation extends AbstractCommand {

	private final TextOperation command;

	public SelectionBasedTextOperation(TextOperation command) {
		this.command = command;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor) {
		command.execute(editorAdaptor, editorAdaptor.getSelection(), ContentType.TEXT);
	}

	@Override
	public Command repetition() {
		TextOperation wrappedRepetition = command.repetition();
		if (wrappedRepetition != null)
			return new SelectionBasedTextOperation(wrappedRepetition);
		return null;
	}

	@Override
	public Command withCount(int count) {
		return this; // ignore count
	}

}
