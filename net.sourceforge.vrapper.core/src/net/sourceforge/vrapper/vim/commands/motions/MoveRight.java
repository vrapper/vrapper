package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;

public class MoveRight extends LeftRightMotion {

	@Override
	protected int destination(int offset, TextContent content, int count) {
	    int lineEnd = content.getLineInformationOfOffset(offset).getEndOffset();
		return Math.min(lineEnd, offset+count);
	}

}
