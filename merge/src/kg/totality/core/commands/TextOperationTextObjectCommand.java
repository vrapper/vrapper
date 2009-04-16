package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.utils.ContentType;
import newpackage.position.TextRange;

public class TextOperationTextObjectCommand extends TextObjectCommand {

	private final TextOperation command;

	public TextOperationTextObjectCommand(TextOperation command, TextObject textObject) {
		super(textObject);
		this.command = command;
	}

	@Override
	protected void execute(EditorAdaptor editorMode, TextRange range, ContentType contentType) {
		command.execute(editorMode, range, contentType);
	}

	@Override
	public CountAwareCommand repetition() {
		TextOperation wrappedRepetition = command.repetition();
		if (wrappedRepetition != null)
			return new TextOperationTextObjectCommand(wrappedRepetition, textObject);
		return null;
	}

}
