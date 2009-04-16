package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;

public class CountedCommand implements Command {

	private final int count;
	protected final CountAwareCommand command;

	public CountedCommand(int count, CountAwareCommand command) {
		this.count = count;
		this.command = command;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor) {
		command.execute(editorAdaptor, count);
	}

	@Override
	public Command repetition() {
		CountAwareCommand repetition = command.repetition();
		if (repetition != null)
			return new CountedCommand(count, repetition);
		return repetition;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public Command withCount(int count) {
		return new CountedCommand(count, command);
	}

}
