package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;

public class MoveToColumn extends LeftRightMotion {

    public static final MoveToColumn INSTANCE = new MoveToColumn();

    private MoveToColumn() { /* NOP */ }

	@Override
	protected int destination(int offset, TextContent content, int count) {
		LineInformation line = content.getLineInformationOfOffset(offset);
		int lineStart = line.getBeginOffset() -1;
		int lineEnd = line.getEndOffset();
	    
		return Math.min(lineEnd, lineStart+count);
	}

}
