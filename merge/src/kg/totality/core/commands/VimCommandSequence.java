package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;

public class VimCommandSequence extends SimpleRepeatableCommand {

	private Command[] commands;

	public VimCommandSequence(Command[] commands) {
		this.commands = commands;
	}

	@Override
	public void execute(EditorAdaptor editorMode) {
		for (Command command: commands)
			command.execute(editorMode);
	}

	@Override
	public Command repetition() {
		Command[] repeated = new CountAwareCommand[commands.length];
		for (int i = 0; i < commands.length; i++) {
			Command rep = commands[i].repetition();
			if (rep != null)
				repeated[i] = rep;
			else
				repeated[i] = commands[i];
		}
		return new VimCommandSequence(repeated);
	}

}
