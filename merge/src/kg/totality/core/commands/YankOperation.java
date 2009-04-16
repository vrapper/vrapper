package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.utils.ContentType;
import newpackage.position.TextRange;
import newpackage.vim.register.RegisterContent;
import newpackage.vim.register.StringRegisterContent;

public class YankOperation implements TextOperation {

	@Override
	public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType) {
		doIt(editorAdaptor, region, contentType);
	}

	@Override
	public TextOperation repetition() {
		return null;
	}

	public static void doIt(EditorAdaptor editorAdaptor, TextRange range, ContentType contentType) {
		String text = editorAdaptor.getModelContent().getText(range.getLeftBound().getModelOffset(), range.getModelLength());
		RegisterContent content = new StringRegisterContent(contentType, text);
		editorAdaptor.getRegisterManager().getActiveRegister().setContent(content);
	}

}
