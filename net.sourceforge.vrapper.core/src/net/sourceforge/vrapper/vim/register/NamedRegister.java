package net.sourceforge.vrapper.vim.register;

public class NamedRegister extends SimpleRegister {

    private final Register unnamed;

    public NamedRegister(Register unnamed) {
        super();
        this.unnamed = unnamed;
    }

    @Override
    public void setContent(RegisterContent content) {
        super.setContent(content);
        unnamed.setContent(content);
    }

}
