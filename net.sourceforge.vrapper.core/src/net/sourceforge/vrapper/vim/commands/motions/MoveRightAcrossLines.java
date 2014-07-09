package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;

public class MoveRightAcrossLines extends LeftRightMotion {

    public static final Motion INSTANCE = new MoveRightAcrossLines(true);
    public static final Motion INSTANCE_BEHIND_CHAR = new MoveRightAcrossLines(false);

    private boolean skipLastChar;

    private MoveRightAcrossLines(boolean skipLastChar) {
        this.skipLastChar = skipLastChar;
    }
    
    @Override
	protected int destination(int offset, TextContent content, int count) {
		int len = content.getTextLength();
		int result = Math.min(len, offset+count);
		LineInformation lineInformation = content.getLineInformationOfOffset(result);
		if (result < len
		        && (result > lineInformation.getEndOffset()
		                || (skipLastChar && result == lineInformation.getEndOffset()))) {
			result = content.getLineInformation(lineInformation.getNumber() + 1).getBeginOffset();
		}
		return result;
	}

}
