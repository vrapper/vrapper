package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

/**
 * A simple implementation of {@link Repeatable} which just evaluates and then
 * executes itself a number of times.
 *
 * @author Matthias Radig
 */
public abstract class AbstractRepeatable extends AbstractToken implements
Repeatable {

    private Token subject;
    private int times;

    public AbstractRepeatable() {
        super();
    }

    public boolean repeat(VimEmulator vim, final int times, Token next)
    throws TokenException {
        subject = next;
        this.times = times;
        return true;
    }

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        subject = next;
        this.times = 1;
        return true;
    }

    protected abstract Action createAction();

    public Action getAction() {
        return new Action() {

            public void execute(VimEmulator vim) {
                for (int i = 0; i < times; i++) {
                    try {
                        evaluate(vim, subject);
                    } catch (TokenException e) {
                        // cannot repeat anymore
                        break;
                    }
                    createAction().execute(vim);
                }
            }

        };
    }

}