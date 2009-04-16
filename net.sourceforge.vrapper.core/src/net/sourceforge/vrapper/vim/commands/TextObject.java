package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public interface TextObject extends Counted<TextObject> {
	@Deprecated
	public TextRange getRegion(EditorAdaptor editorMode);
	public TextRange getRegion(EditorAdaptor editorMode, int count);
	public ContentType getContentType();
}
