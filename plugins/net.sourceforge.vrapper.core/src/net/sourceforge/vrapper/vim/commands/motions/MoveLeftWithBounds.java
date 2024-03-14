package net.sourceforge.vrapper.vim.commands.motions;

import static java.lang.Math.max;
import static java.lang.Math.min;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.VimUtils;

public abstract class MoveLeftWithBounds extends MoveWithBounds {

	public MoveLeftWithBounds(boolean bailOff) {
        super(bailOff);
    }

    @Override
	protected int destination(int offset, TextContent content, boolean bailOff, boolean hasMoreCounts) {
		boolean haveMoved = false;
		// special case - end of buffer
		final int last = content.getTextLength() - 1;
		if (offset > last) {
            if (atBoundary(content.getText(last, 1).charAt(0), ' ')) {
                return last;
            } else {
				haveMoved = true;
				--offset;
			}
        }

		// move or bail off
		if (!bailOff && !haveMoved && !shouldStopAtLeftBoundingChar()) {
            --offset;
        }

		boolean lookingAtNL = false;
		notFound: while (offset >= 1) {
			int i, len = min(BUFFER_LEN, offset + 1);
			String buffer = content.getText(offset + 1 - len, len);
			for (i = len-1; i > 0; i--, offset--) {
				if (atBoundary(buffer.charAt(i-1), buffer.charAt(i))) {
                    break notFound;
                }
				if (stopsAtNewlines()) {
				    String prefix = buffer.substring(0, i + (shouldStopAtLeftBoundingChar() ? 0 : 1));
				    int nlSkip = VimUtils.endsWithNewLine(prefix);
				    if (nlSkip != 0) {
				        if (lookingAtNL) {
				            ++offset;
				            break notFound;
				        } else {
				            i -= nlSkip - 1;
				            offset -= nlSkip - 1;
				        }
				    } 
				    lookingAtNL = nlSkip != 0;
				}
			}
		}

		if (shouldStopAtLeftBoundingChar()) {
            --offset;
        }

		return max(0, offset);
	}
}