package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public interface TextOperation {

	void execute(EditorAdaptor editorAdaptor, TextRange range, ContentType contentType);

	TextOperation repetition();

}
