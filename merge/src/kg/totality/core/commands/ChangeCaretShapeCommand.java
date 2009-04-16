package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.utils.CaretType;

public class ChangeCaretShapeCommand extends CountIgnoringNonRepeatableCommand {

	private final CaretType caretType;

	public ChangeCaretShapeCommand(CaretType caretType) {
		this.caretType = caretType;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor) {
		editorAdaptor.getCursorService().setCaret(caretType);
	}

}
