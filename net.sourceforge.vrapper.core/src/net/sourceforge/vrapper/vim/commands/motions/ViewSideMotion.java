package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class ViewSideMotion implements Motion {

	@Override
	public Position destination(EditorAdaptor editorAdaptor) {
		TextContent content = editorAdaptor.getViewContent();
		Position position = editorAdaptor.getPosition();
		int offset = position.getViewOffset();
		return position.addViewOffset(getJump(offset, content));
	}

	protected abstract int getJump(int offset, TextContent content);

}
