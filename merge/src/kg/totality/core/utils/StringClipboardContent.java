package kg.totality.core.utils;



public class StringClipboardContent implements ClipboardContent {

	private final String text;
	private final ContentType contentType;

	public StringClipboardContent(ContentType contentType, String textRange) {
		this.contentType = contentType;
		text = textRange;
	}

	@Override
	public String asText() {
		return text;
	}

	@Override
	public ContentType type() {
		return contentType;
	}

}
