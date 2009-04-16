package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.utils.ContentType;
import newpackage.position.TextRange;

public interface TextObject extends Counted<TextObject> {
	public TextRange getRegion(EditorAdaptor editorMode);
	public TextRange getRegion(EditorAdaptor editorMode, int count);
	public ContentType getContentType();
}
