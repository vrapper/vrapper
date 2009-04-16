package kg.totality.core.commands.motions;

public class MoveWORDEndLeft extends MoveWordEndLeft {

	@Override
	protected boolean atBoundary(char c1, char c2) {
		return !Character.isWhitespace(c1) && Character.isWhitespace(c2);
	}

}
