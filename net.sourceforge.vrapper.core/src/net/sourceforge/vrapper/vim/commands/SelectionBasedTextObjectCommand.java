package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SelectionBasedTextObjectCommand extends TextObjectCommand {
	protected Command command;

	public SelectionBasedTextObjectCommand(Command command, TextObject textObject) {
		super(textObject);
		this.command = command;
	}

	@Override
	protected void execute(EditorAdaptor editorMode, TextRange range, ContentType contentType) throws CommandExecutionException {
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

