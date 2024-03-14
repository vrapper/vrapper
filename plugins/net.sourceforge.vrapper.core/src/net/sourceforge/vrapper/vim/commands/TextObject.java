package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public interface TextObject extends Counted<TextObject> {
	/**
	 * Get the range of text which would be selected by this text object.
	 * @return a {@link TextRange} instance or <tt>null</tt> in case a text selection would make
	 *     no sense whatsoever (some operations may act differently in this case, otherwise they\
	 *     would only see an zero-length range).
	 */
	public TextRange getRegion(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException;
	/**
	 * Returns the content type of the text selected by this text object.
	 */
	public ContentType getContentType(Configuration configuration);
}
