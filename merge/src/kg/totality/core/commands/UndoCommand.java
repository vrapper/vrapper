package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;

public class UndoCommand extends SimpleRepeatableCommand {

	@Override
	public void execute(EditorAdaptor editorAdaptor) {
		editorAdaptor.getHistory().undo();
	}

	@Override
	public CountAwareCommand repetition() {
		return null;
	}

}
