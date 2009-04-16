package kg.totality.core.commands.motions;

import static kg.totality.core.commands.Utils.characterType;
import kg.totality.core.commands.BorderPolicy;

public class MoveWordEndLeft extends MoveLeftWithBounds {

	@Override
	protected boolean atBoundary(char c1, char c2) {
		return !Character.isWhitespace(c1) && characterType(c1) != characterType(c2);
	}

	@Override
	public BorderPolicy borderPolicy() {
		return BorderPolicy.INCLUSIVE;
	}

	@Override
	protected boolean shouldStopAtLeftBoundingChar() {
		return true;
	}

	@Override
	protected boolean stopsAtNewlines() {
		return true;
	}

}
