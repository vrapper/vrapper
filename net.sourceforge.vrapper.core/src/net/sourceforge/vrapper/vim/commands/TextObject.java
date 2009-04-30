package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public interface TextObject extends Counted<TextObject> {
	public TextRange getRegion(EditorAdaptor editorMode, int count) throws CommandExecutionException;
	public ContentType getContentType();
}
