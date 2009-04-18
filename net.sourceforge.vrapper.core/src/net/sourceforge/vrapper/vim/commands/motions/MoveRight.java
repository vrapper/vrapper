package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;

public class MoveRight extends LeftRightMotion {

	@Override
	protected int destination(int offset, TextContent content, int count) {
		int to = Math.min(offset + count, content.getTextLength());
		String text = content.getText(offset, to - offset);
		int i = text.indexOf('\n');
		return i == -1 ? to : offset + i;
	}

}
