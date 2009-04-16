package kg.totality.core.commands;

import static kg.totality.core.commands.ConstructorWrappers.editText;
import kg.totality.core.EditorAdaptor;
import kg.totality.core.modes.InsertMode;
import kg.totality.core.utils.ContentType;
import newpackage.position.TextRange;

public class ChangeOperation implements TextOperation {

	@Override
	public void execute(EditorAdaptor editorAdaptor, TextRange range, ContentType contentType) {
		// Will get unlocked and finished when insert mode is left
		// XXX: this is a little fragile, but probably there is no better way of doing it
		InsertMode.inChange = true; // FIXME: this is test only
		editorAdaptor.getHistory().beginCompoundChange();
		editorAdaptor.getHistory().lock();

		DeleteOperation.doIt(editorAdaptor, range, contentType);
		editorAdaptor.changeMode(InsertMode.NAME);
		if (contentType == ContentType.LINES)
			editText("smartEnterInverse").execute(editorAdaptor); // FIXME: user Vrapper's code
	}

	@Override
	public TextOperation repetition() {
		return new ChangeToLastEditOperation();
	}
}
