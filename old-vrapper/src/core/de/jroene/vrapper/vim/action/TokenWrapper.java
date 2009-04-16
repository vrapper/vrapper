package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.token.Delete;
import de.jroene.vrapper.vim.token.Move;
import de.jroene.vrapper.vim.token.Token;
import de.jroene.vrapper.vim.token.TokenException;

/**
 * Wraps a {@link Token} so that it can be handled as if it was just an action.
 * Can only work with tokens which do not need other tokens to work (like {@link Delete}
 * needs some {@link Move} to do something useful.)
 *
 * @author Matthias Radig
 */
public class TokenWrapper implements Action {

    private final Token token;

    public TokenWrapper(Token token) {
        super();
        this.token = token;
    }
    public void execute(VimEmulator vim) {
        try {
            token.evaluate(vim, null);
            token.getAction().execute(vim);
        } catch (TokenException e) {
            throw new RuntimeException(e);
        }
    }

}
