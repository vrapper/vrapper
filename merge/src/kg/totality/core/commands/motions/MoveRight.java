package kg.totality.core.commands.motions;

import newpackage.glue.TextContent;

public class MoveRight extends LeftRightMotion {

	@Override
	protected int destination(int offset, TextContent content, int count) {
		int to = Math.min(offset + count, content.getTextLength() - 1);
		String text = content.getText(offset, to - offset + 1);
		int i = 0;
		for (; i < text.length() - 1; i++)
			if (text.charAt(i) == '\n') // FIXME: Windows?
				break;
		return offset + i;
	}

}
