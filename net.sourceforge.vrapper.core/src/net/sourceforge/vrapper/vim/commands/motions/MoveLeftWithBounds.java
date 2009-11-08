package net.sourceforge.vrapper.vim.commands.motions;

import static java.lang.Math.max;
import static java.lang.Math.min;
import net.sourceforge.vrapper.platform.TextContent;

public abstract class MoveLeftWithBounds extends MoveWithBounds {

	@Override
	protected int destination(int offset, TextContent content) {
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

		// always move
		if (!haveMoved && !shouldStopAtLeftBoundingChar()) {
            --offset;
        }

		notFound: while (offset >= 1) {
			int i, len = min(BUFFER_LEN, offset + 1);
			String buffer = content.getText(offset + 1 - len, len);
			for (i = len-1; i > 0; i--, offset--) {
				if (stopsAtNewlines() && buffer.charAt(i-1) == '\n' && buffer.charAt(i) == '\n') {
                    return max(0, offset);
                }
				if (atBoundary(buffer.charAt(i-1), buffer.charAt(i))) {
                    break notFound;
                }
			}
		}

		if (shouldStopAtLeftBoundingChar()) {
            --offset;
        }

		return max(0, offset);
	}
}