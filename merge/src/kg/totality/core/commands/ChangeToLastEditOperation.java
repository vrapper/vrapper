package kg.totality.core.commands;

import static kg.totality.core.commands.ConstructorWrappers.editText;
import kg.totality.core.EditorAdaptor;
import kg.totality.core.utils.ContentType;
import newpackage.position.TextRange;

public class ChangeToLastEditOperation implements TextOperation {
	@Override
	public void execute(EditorAdaptor editorAdapter, TextRange range, ContentType contentType) {
		editorAdapter.getHistory().beginCompoundChange();
		DeleteOperation.doIt(editorAdapter, range, contentType);
		if (contentType == ContentType.LINES)
			editText("smartEnterInverse").execute(editorAdapter); // FIXME: use Vrapper's code
		RepeatLastInsertCommand.doIt(editorAdapter, null, 1);
		editorAdapter.getHistory().endCompoundChange();
	}

	@Override
	public TextOperation repetition() {
		return this;
	}
}
