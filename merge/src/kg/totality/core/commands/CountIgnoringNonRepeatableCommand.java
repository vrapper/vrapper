package kg.totality.core.commands;


public abstract class CountIgnoringNonRepeatableCommand implements Command {
	@Override
	public Command repetition() {
		return null;
	}

	@Override
	public Command withCount(int count) {
		return this;
	}

	@Override
	public int getCount() {
		return NO_COUNT_GIVEN;
	}

}
