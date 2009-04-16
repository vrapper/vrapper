package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.utils.ContentType;
import newpackage.position.TextRange;

public abstract class TextObjectCommand extends CountAwareCommand {
	protected TextObject textObject;

	protected abstract void execute(EditorAdaptor editorMode, TextRange range, ContentType contentType);

	public TextObjectCommand(TextObject textObject) {
		this.textObject = textObject;
	}

	@Override
	public void execute(EditorAdaptor editorMode, int count) {
		TextRange range = textObject.getRegion(editorMode, count);
		ContentType contentType = textObject.getContentType();
		execute(editorMode, range, contentType);
	}

	@Override
	public Command withCount(int count) {
		return new MultiplicableCountedCommand(count, this);
	}
}

