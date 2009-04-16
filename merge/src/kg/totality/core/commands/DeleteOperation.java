package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.utils.ContentType;
import newpackage.glue.TextContent;
import newpackage.position.TextRange;

public class DeleteOperation implements TextOperation {

	@Override
	public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType) {
		doIt(editorAdaptor, region, contentType);
	}

	@Override
	public TextOperation repetition() {
		return this;
	}

	public static void doIt(EditorAdaptor editorAdaptor, TextRange range, ContentType contentType) {
		YankOperation.doIt(editorAdaptor, range, contentType);
		TextContent content = editorAdaptor.getModelContent();
		if (editorAdaptor.getFileService().isEditable()) // TODO: test
			content.replace(range.getLeftBound().getModelOffset(), range.getModelLength(), "");
	}
}
