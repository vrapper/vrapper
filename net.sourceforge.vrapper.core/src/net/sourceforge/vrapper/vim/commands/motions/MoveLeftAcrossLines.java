package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;

public class MoveLeftAcrossLines extends LeftRightMotion {

    public static final MoveLeftAcrossLines INSTANCE = new MoveLeftAcrossLines();

    private MoveLeftAcrossLines() { /* NOP */ }

	@Override
	protected int destination(int offset, TextContent content, int count) {
		int result = Math.max(0, offset - count);
		LineInformation lineInformation = content.getLineInformationOfOffset(offset);
		if (result < lineInformation.getBeginOffset()) {
			LineInformation resultLine = content.getLineInformation(lineInformation.getNumber() - 1);
			result = resultLine.getEndOffset();
		}
		return result;
	}
}
