package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.utils.ContentType;
import newpackage.position.TextRange;

public class MultipliedTextObject implements TextObject {

	private final int count;
	private final TextObject textObject;

	public MultipliedTextObject(int count, TextObject textObject) {
		this.count = count;
		this.textObject = textObject;
	}

	@Override
	public ContentType getContentType() {
		return textObject.getContentType();
	}

	@Override
	public TextRange getRegion(EditorAdaptor editorMode) {
		return textObject.getRegion(editorMode, count);
	}

	@Override
	public TextRange getRegion(EditorAdaptor editorMode, int count) {
		return textObject.getRegion(editorMode, count);
	}

	@Override
	public TextObject withCount(int count) {
		return new MultipliedTextObject(count, textObject);
	}

	@Override
	public int getCount() {
		return count;
	}

}
