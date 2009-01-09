package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.token.AbstractToken;
import de.jroene.vrapper.vim.token.Token;

/**
 * A simple implementation of {@link Token} which just returns itself as action
 * and does not interact with other tokens.
 *
 * @author Matthias Radig
 */
public abstract class TokenAndAction extends AbstractToken implements Action, Token {

    public boolean evaluate(VimEmulator vim, Token next) {
        return true;
    }

    public Action getAction() {
        return this;
    }

    public Space getSpace() {
        return Space.MODEL;
    }

    @Override
    public boolean isOperator() {
        return false;
    }

}
