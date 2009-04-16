package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;

public class MultipleExecutionCommand implements Command {

	private final int count;
	private final Command command;

	public MultipleExecutionCommand(int count, Command command) {
		this.count = count;
		this.command = command;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor) {
		for (int i = 0; i < count; i++)
			command.execute(editorAdaptor);
	}

	@Override
	public Command repetition() {
		Command repetition = command.repetition();
		if (repetition != null)
			return new MultipleExecutionCommand(count, repetition);
		return null;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public Command withCount(int count) {
		return new MultipleExecutionCommand(count, command);
	}

}
