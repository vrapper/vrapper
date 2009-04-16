package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import newpackage.position.StartEndTextRange;
import newpackage.position.TextRange;

public class SwapSelectionSidesCommand extends CountIgnoringNonRepeatableCommand {

	@Override
	public void execute(EditorAdaptor editorAdaptor) {
		TextRange selection = editorAdaptor.getSelection();
		editorAdaptor.setPosition(selection.getEnd(), true);
		editorAdaptor.setSelection(new StartEndTextRange(selection.getEnd(), selection.getStart()));
	}

}
