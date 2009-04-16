package newpackage.vim.register;

import kg.totality.core.utils.ContentType;
import de.jroene.vrapper.vim.VimConstants;

public class AppendRegister implements Register {

    private final Register delegate;

    public AppendRegister(Register delegate) {
        this.delegate = delegate;
    }

    public RegisterContent getContent() {
        return delegate.getContent();
    }

    public void setContent(RegisterContent content) {
    	// TODO: keystroke content type
        RegisterContent oldContent = getContent();
        ContentType type;
        if (oldContent.getPayloadType() == content.getPayloadType())
        	type = content.getPayloadType();
        else if (oldContent.getPayloadType() == ContentType.LINES || content.getPayloadType() == ContentType.LINES)
        	type = ContentType.LINES;
		else
			type = ContentType.TEXT;
        StringBuilder sb = new StringBuilder();
        sb.append(oldContent.getText());
        if (type == ContentType.LINES) {
            // TODO: use correct newline
            sb.append(VimConstants.REGISTER_NEWLINE);
        }
        sb.append(content.getText());
        RegisterContent newContent = new StringRegisterContent(type, sb.toString());
        delegate.setContent(newContent);
    }

}
