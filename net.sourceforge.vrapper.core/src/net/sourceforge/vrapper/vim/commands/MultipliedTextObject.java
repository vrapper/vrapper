package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

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
