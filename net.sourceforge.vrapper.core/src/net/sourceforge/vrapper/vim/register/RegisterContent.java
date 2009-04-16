package net.sourceforge.vrapper.vim.register;

import net.sourceforge.vrapper.utils.ContentType;

public interface RegisterContent {
    public static final RegisterContent DEFAULT_CONTENT = new StringRegisterContent(ContentType.TEXT, "");

	ContentType getPayloadType();
	String getText();
}
