package net.sourceforge.vrapper.vim.register;


public abstract class ReadOnlyRegister implements Register {

    @Override
    public void setContent(RegisterContent content, boolean copyToUnnamed) {
        // do nothing
    }

    public void setContent(RegisterContent content) {
        setContent(content, true);
    }

}
