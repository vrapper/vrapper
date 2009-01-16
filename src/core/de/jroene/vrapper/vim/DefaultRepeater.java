/**
 * 
 */
package de.jroene.vrapper.vim;

import de.jroene.vrapper.vim.action.Action;
import de.jroene.vrapper.vim.token.AbstractToken;
import de.jroene.vrapper.vim.token.Repeatable;
import de.jroene.vrapper.vim.token.Token;
import de.jroene.vrapper.vim.token.TokenException;

public class DefaultRepeater extends AbstractToken implements Repeatable {

    private final int defaultTimes;
    private final Repeatable token;

    public DefaultRepeater(int defaultTimes, Repeatable token) {
        super();
        this.token = token;
        this.defaultTimes = defaultTimes;
    }

    public boolean repeat(VimEmulator vim, int times, Token next)
    throws TokenException {
        return token.repeat(vim, times, next);
    }

    public boolean evaluate(VimEmulator vim, Token next)
    throws TokenException {
        return token.repeat(vim, defaultTimes, next);
    }

    public Action getAction() {
        return token.getAction();
    }

    public Space getSpace() {
        return token.getSpace();
    }
}