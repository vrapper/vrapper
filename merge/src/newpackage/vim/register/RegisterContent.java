package newpackage.vim.register;

import kg.totality.core.utils.ContentType;

public interface RegisterContent {
    public static final RegisterContent DEFAULT_CONTENT = new StringRegisterContent(ContentType.TEXT, "");

	ContentType getPayloadType();
	String getText();
}
