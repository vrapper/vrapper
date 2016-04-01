package net.sourceforge.vrapper.vim.register;

public class NamedRegister extends SimpleRegister {

    private final Register unnamed;

    public NamedRegister(Register unnamed) {
        super();
        this.unnamed = unnamed;
    }

    @Override
    public void setContent(RegisterContent content, boolean copyToUnnamed) {
        super.setContent(content, copyToUnnamed);
        if (copyToUnnamed) {
            unnamed.setContent(content);
        }
    }

}
