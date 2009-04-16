package kg.totality.core.commands.motions;

import kg.totality.core.commands.BorderPolicy;
import newpackage.glue.TextContent;
import de.jroene.vrapper.vim.LineInformation;

public class LineStartMotion extends AbstractModelSideMotion {

	private final boolean goToFirstNonWS;

	public LineStartMotion(boolean goToFirstNonWS) {
		this.goToFirstNonWS = goToFirstNonWS;
	}

	@Override
	public BorderPolicy borderPolicy() {
		return BorderPolicy.EXCLUSIVE;
	}

	@Override
	protected int destination(int position, TextContent content, int count) {
		// note: it ignores count, because that's what Vim's '^' and '0' motions do
		// (well, '^' does, try to do counted '0' ;-])
		LineInformation lineInfo = content.getLineInformationOfOffset(position);
		int result = lineInfo.getBeginOffset();
		if (goToFirstNonWS) {
			String line = content.getText(lineInfo.getBeginOffset(), lineInfo.getLength());
			int indent;
			for (indent = 0; indent < line.length(); indent++)
				if (!Character.isWhitespace(line.charAt(indent)))
					break;
			result += indent;
		}
		return result;
}

}
