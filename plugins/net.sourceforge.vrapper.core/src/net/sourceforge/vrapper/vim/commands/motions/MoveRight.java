package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;

public class MoveRight extends LeftRightMotion {

    public static final Motion INSTANCE = new MoveRight();

    private MoveRight() { /* NOP */ }

	@Override
	protected int destination(int offset, TextContent content, int count) {
	    int lineEnd = content.getLineInformationOfOffset(offset).getEndOffset();
		return Math.min(lineEnd, offset+count);
	}

}
