package net.sourceforge.vrapper.vim.commands.motions;

import static java.lang.Math.min;
import net.sourceforge.vrapper.platform.TextContent;

public abstract class MoveRightWithBounds extends MoveWithBounds {

	public MoveRightWithBounds(boolean bailOff) {
        super(bailOff);
    }

    @Override
	protected int destination(int offset, TextContent content, boolean bailOff) {
		// ensure we don't stay inside object
		if (!bailOff && shouldStopAtLeftBoundingChar())
			++offset;

		int textLen = content.getTextLength();
		notFound: while (offset < textLen - 1) {
			int i, len = min(BUFFER_LEN, textLen - offset);
			String buffer = content.getText(offset, len);
			for (i = 0; i < len-1; i++, offset++) {
				if (stopsAtNewlines() && buffer.charAt(i) == '\n' && buffer.charAt(i+1) == '\n') // TODO: test on Windows (\r\n)
					return min(offset+1, textLen);
				if (atBoundary(buffer.charAt(i), buffer.charAt(i+1)))
					break notFound;
			}
		}

		if (!shouldStopAtLeftBoundingChar())
			++offset;

		return min(offset, textLen);
	}

}