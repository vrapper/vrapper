package de.jroene.vrapper.vim.token;

/**
 * Like {@link AbstractRepeatableMove} but always vertical.
 *
 * @author Matthias Radig
 */
public abstract class AbstractRepeatableVerticalMove extends AbstractRepeatableMove {

    @Override
    public final boolean isHorizontal() {
        return false;
    }

}
