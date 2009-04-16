package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;

public class RedoCommand extends SimpleRepeatableCommand {

	@Override
	public void execute(EditorAdaptor editorAdaptor) {
		editorAdaptor.getHistory().redo();
	}

	@Override
	public CountAwareCommand repetition() {
		return null;
	}

}
