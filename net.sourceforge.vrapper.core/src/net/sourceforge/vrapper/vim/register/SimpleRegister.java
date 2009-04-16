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

    public void setContent(RegisterContent content) {
        this.content = content;
    }
}
