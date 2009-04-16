package net.sourceforge.vrapper.vim.register;

import net.sourceforge.vrapper.utils.ContentType;

/**
 * Holds information for delete, yank, put operations.
 *
 * @author Matthias Radig
 */
public class StringRegisterContent implements RegisterContent {

    private final String payload;
	private final ContentType contentType;
    public StringRegisterContent(ContentType contentType, String payload) {
        this.contentType = contentType;
		this.payload = payload;
    }

    @Override
    public ContentType getPayloadType() {
		return contentType;
	}

    @Override
    public String getText() {
        return payload;
    }

}
