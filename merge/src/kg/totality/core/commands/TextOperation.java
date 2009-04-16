package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.utils.ContentType;
import newpackage.position.TextRange;

public interface TextOperation {

	void execute(EditorAdaptor editorAdaptor, TextRange range, ContentType contentType);

	TextOperation repetition();

}
