package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;

public class MoveRightAcrossLines extends LeftRightMotion {

    public static final MoveRightAcrossLines INSTANCE = new MoveRightAcrossLines();

    private MoveRightAcrossLines() { /* NOP */ }

	@Override
	protected int destination(int offset, TextContent content, int count) {
		int len = content.getTextLength();
        int result = Math.min(len, offset+count);
		if (result < len && result == content.getLineInformationOfOffset(result).getEndOffset())
		    ++result;
		return result;
	}

}
