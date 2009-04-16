package de.jroene.vrapper.vim;

/**
 * Abstract {@link Mode} implementation for convenience.
 *
 * @author Matthias Radig
 */
public abstract class AbstractMode implements Mode {

    protected final VimEmulator vim;

    public AbstractMode(VimEmulator vim) {
        super();
        this.vim = vim;
    }

}