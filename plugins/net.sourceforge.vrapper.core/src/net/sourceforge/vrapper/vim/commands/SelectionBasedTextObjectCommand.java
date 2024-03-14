package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Temporarily changes the current selection to the range returned by a TextObject.
 * <p>
 * Currently used to run Eclipse commands on TextObjects. Native Eclipse commands can only check the
 * editor selection, they know nothing about Vrapper's richer objects.
 */
public class SelectionBasedTextObjectCommand extends TextObjectCommand {
	protected Command command;

	public SelectionBasedTextObjectCommand(Command command, TextObject textObject) {
		super(textObject);
		this.command = command;
	}

	@Override
	protected void execute(EditorAdaptor editorMode, TextRange range, ContentType contentType) throws CommandExecutionException {
		// TODO: move cursor - compatibility option
		editorMode.setSelection(new SimpleSelection(range));
		command.execute(editorMode);
		// Eclipse commands already clear selection, but let's just be sure.
		editorMode.setSelection(null);
	}

	@Override
	public CountAwareCommand repetition() {
		Command wrappedRepetition = command.repetition();
		if (wrappedRepetition != null) {
            return new SelectionBasedTextObjectCommand(wrappedRepetition, textObject);
        }
		return null;
	}

}

