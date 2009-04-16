package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.utils.ContentType;
import newpackage.position.TextRange;

public class SelectionBasedTextObjectCommand extends TextObjectCommand {
	protected Command command;

	public SelectionBasedTextObjectCommand(Command command, TextObject textObject) {
		super(textObject);
		this.command = command;
	}

	@Override
	protected void execute(EditorAdaptor editorMode, TextRange range, ContentType contentType) {
		// TODO: move cursor - compatibility option
		editorMode.setSelection(range);
		command.execute(editorMode);
	}

	@Override
	public CountAwareCommand repetition() {
		Command wrappedRepetition = command.repetition();
		if (wrappedRepetition != null)
			return new SelectionBasedTextObjectCommand(wrappedRepetition, textObject);
		return null;
	}

}

