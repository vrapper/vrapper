package de.jroene.vrapper.vim.register;

/**
 * Simplest possible implementation of a register.
 *
 * @author Matthias Radig
 */
public class SimpleRegister implements Register {

    private static final RegisterContent DEFAULT_CONTENT = new RegisterContent(false, "");
    private RegisterContent content = DEFAULT_CONTENT;

    public RegisterContent getContent() {
        return content;
    }

    public void setContent(RegisterContent content) {
        this.content = content;
    }
}
