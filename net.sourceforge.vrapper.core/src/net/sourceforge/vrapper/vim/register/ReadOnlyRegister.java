package net.sourceforge.vrapper.vim.register;


public abstract class ReadOnlyRegister implements Register {

    public void setContent(RegisterContent content) {
        // do nothing
    }

}
