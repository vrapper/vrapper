package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class TextObjectCommand extends CountAwareCommand {
	protected TextObject textObject;

	protected abstract void execute(EditorAdaptor editorMode, TextRange range, ContentType contentType) throws CommandExecutionException;

	public TextObjectCommand(TextObject textObject) {
		this.textObject = textObject;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
		TextRange range = textObject.getRegion(editorAdaptor, count);
		ContentType contentType = textObject.getContentType(editorAdaptor.getConfiguration());
		execute(editorAdaptor, range, contentType);
	}

	@Override
	public Command withCount(int count) {
		return new MultiplicableCountedCommand(count, this);
	}
}

