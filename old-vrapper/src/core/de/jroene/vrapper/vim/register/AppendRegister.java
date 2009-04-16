package de.jroene.vrapper.vim.register;

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
        RegisterContent oldContent = getContent();
        boolean lineWise = oldContent.isLineWise() || content.isLineWise();
        StringBuilder sb = new StringBuilder();
        sb.append(oldContent.getPayload());
        if (lineWise) {
            // TODO: use correct newline
            sb.append(VimConstants.REGISTER_NEWLINE);
        }
        sb.append(content.getPayload());
        RegisterContent newContent = new RegisterContent(
                lineWise, sb.toString());
        delegate.setContent(newContent);
    }

}
