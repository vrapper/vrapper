package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.VimEmulator;

/**
 * {@link AbstractMove} implementation which can be repeated.
 *
 * @author Matthias Radig
 */
public abstract class AbstractRepeatableMove extends AbstractMove implements RepeatableMove {

    public AbstractRepeatableMove() {
        super();
    }

    abstract public int calculateTarget(VimEmulator vim, int times, Token next);

    public boolean repeat(VimEmulator vim, int times, Token next) throws TokenException {
        setTarget(calculateTarget(vim, times, next));
        if (getTarget() == -1) {
            throw new TokenException();
        }
        return true;
    }

    @Override
    protected int calculateTarget(VimEmulator vim, Token next) {
        return calculateTarget(vim, 1, next);
    }

}