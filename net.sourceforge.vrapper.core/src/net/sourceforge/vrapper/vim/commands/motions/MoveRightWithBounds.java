package net.sourceforge.vrapper.vim.commands.motions;

import static java.lang.Math.min;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.VimUtils;

public abstract class MoveRightWithBounds extends MoveWithBounds {

	public MoveRightWithBounds(boolean bailOff) {
        super(bailOff);
    }

    @Override
	protected int destination(int offset, TextContent content, boolean bailOff, boolean hasMoreCounts) {
		// ensure we don't stay inside object
		if (!bailOff && shouldStopAtLeftBoundingChar())
			++offset;

		int textLen = content.getTextLength();
		boolean lookingAtNL = false;
		notFound: while (offset < textLen - 1) {
			int i, len = min(BUFFER_LEN, textLen - offset);
			String buffer = content.getText(offset, len);
			for (i = 0; i < len-1; i++, offset++) {
				if (stopsAtNewlines()) {
				    int nlSkip = VimUtils.startsWithNewLine(buffer.substring(i));
				    if (nlSkip != 0) {
				        if (lookingAtNL) {
				            return min(offset, textLen);
				        } else {
				            i += nlSkip - 1;
				            offset += nlSkip - 1;
				            if (i >= len - 1) {
				                break;
				            }
				        }
				    } 
				    lookingAtNL = nlSkip != 0;
				}
				if (atBoundary(buffer.charAt(i), buffer.charAt(i+1)))
					break notFound;
			}
		}

		if (!shouldStopAtLeftBoundingChar() || hasMoreCounts)
			++offset;

		return min(offset, textLen);
	}

}