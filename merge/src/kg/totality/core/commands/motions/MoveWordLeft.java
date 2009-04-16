package kg.totality.core.commands.motions;

import static kg.totality.core.commands.Utils.characterType;
import kg.totality.core.commands.BorderPolicy;

public class MoveWordLeft extends MoveLeftWithBounds {

	@Override
	protected boolean atBoundary(char c1, char c2) {
		return !Character.isWhitespace(c2) && (characterType(c1) != characterType(c2));
	}

	@Override
	public BorderPolicy borderPolicy() {
		return BorderPolicy.EXCLUSIVE;
	}

	@Override
	protected boolean shouldStopAtLeftBoundingChar() {
		return false;
	}

	@Override
	protected boolean stopsAtNewlines() {
		return true;
	}

}
