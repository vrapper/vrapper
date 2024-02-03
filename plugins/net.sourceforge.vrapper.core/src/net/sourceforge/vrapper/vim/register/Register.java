package net.sourceforge.vrapper.vim.register;


/**
 * Something which can hold {@link RegisterContent}.
 *
 * @author Matthias Radig
 */
public interface Register {

    void setContent(RegisterContent content);

    void setContent(RegisterContent content, boolean copyToUnnamed);

    RegisterContent getContent();
}
