package net.sourceforge.vrapper.vim.register;

/**
 * Simplest possible implementation of a register.
 *
 * @author Matthias Radig
 */
public class SimpleRegister implements Register {

    private RegisterContent content = RegisterContent.DEFAULT_CONTENT;

    public RegisterContent getContent() {
        return content;
    }

    
    @Override
    public void setContent(RegisterContent content, boolean copyToUnnamed) {
        this.content = content;
    }

    public void setContent(RegisterContent content) {
        setContent(content, true);
    }
}
