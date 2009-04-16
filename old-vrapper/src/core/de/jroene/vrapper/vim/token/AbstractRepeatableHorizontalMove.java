package de.jroene.vrapper.vim.token;

/**
 * Like {@link AbstractRepeatableMove} but always horizontal.
 *
 * @author Matthias Radig
 */
public abstract class AbstractRepeatableHorizontalMove extends AbstractRepeatableMove {

    @Override
    public final boolean isHorizontal() {
        return true;
    }

}
