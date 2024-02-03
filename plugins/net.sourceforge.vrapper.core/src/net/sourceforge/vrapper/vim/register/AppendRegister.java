package net.sourceforge.vrapper.vim.register;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.VimConstants;

public class AppendRegister implements Register {

    private final Register delegate;

    public AppendRegister(Register delegate) {
        this.delegate = delegate;
    }

    public RegisterContent getContent() {
        return delegate.getContent();
    }

    public void setContent(RegisterContent content) {
        setContent(content, true);
    }

    @Override
    public void setContent(RegisterContent content, boolean copyToUnnamed) {
        String newline = VimConstants.REGISTER_NEWLINE;
        // TODO: keystroke content type
        RegisterContent oldContent = getContent();
        ContentType type;
        ContentType oldType = oldContent.getPayloadType();
        ContentType appendedType = content.getPayloadType();
        if (oldType == appendedType)
            type = appendedType;
        else if (oldType == ContentType.LINES || appendedType == ContentType.LINES)
            type = ContentType.LINES;
        else
            type = ContentType.TEXT;
        StringBuilder sb = new StringBuilder();

        sb.append(oldContent.getText());
        if (oldType != ContentType.LINES && type == ContentType.LINES)
            sb.append(newline);

        sb.append(content.getText());
        if (appendedType != ContentType.LINES && type == ContentType.LINES)
            sb.append(newline);

        RegisterContent newContent = new StringRegisterContent(type, sb.toString());
        delegate.setContent(newContent, copyToUnnamed);
    }

}
