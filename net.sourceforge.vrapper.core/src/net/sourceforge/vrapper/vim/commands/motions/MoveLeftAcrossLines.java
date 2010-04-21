package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;

public class MoveLeftAcrossLines extends LeftRightMotion {

    public static final MoveLeftAcrossLines INSTANCE = new MoveLeftAcrossLines();

    private MoveLeftAcrossLines() { /* NOP */ }

	@Override
	protected int destination(int offset, TextContent content, int count) {
		return Math.max(0, offset - count);
	}
}
