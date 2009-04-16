package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;

public class MoveLeft extends LeftRightMotion {
	@Override
	protected int destination(int offset, TextContent content, int count) {
		int from = Math.max(0, offset - count);
		String text = content.getText(from, offset - from);
		String reversed = new StringBuilder(text).reverse().toString();
		int i;
		for (i = 0; i < reversed.length(); i++)
			if (reversed.charAt(i) == '\n') // FIXME: Windows?
				break;
		return offset - i;
	}
}
