package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.utils.ContentType;

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
