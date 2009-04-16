package kg.totality.core.commands;


// TODO: invent better name; this has nothing to do with repetition method
public abstract class SimpleRepeatableCommand extends AbstractCommand {

	@Override
	public Command withCount(int count) {
		return new MultipleExecutionCommand(count, this);
	}

}
