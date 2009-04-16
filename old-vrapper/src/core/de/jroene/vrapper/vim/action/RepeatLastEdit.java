package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.token.AbstractToken;
import de.jroene.vrapper.vim.token.Repeatable;
import de.jroene.vrapper.vim.token.Token;
import de.jroene.vrapper.vim.token.TokenException;

public class RepeatLastEdit extends AbstractToken implements Repeatable {

    private Token token;

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        Token last = vim.getRegisterManager().getLastEdit();
        if (last == null) {
            throw new TokenException();
        }
        if (!last.evaluate(vim, next)) {
            throw new IllegalStateException("last edit evaluates as false");
        }
        token = last;
        return true;
    }

    public boolean repeat(VimEmulator vim, int times, Token next)
    throws TokenException {
        Token last = vim.getRegisterManager().getLastEdit();
        if (last == null || !(last instanceof Repeatable)) {
            throw new TokenException();
        }
        if (!((Repeatable)last).repeat(vim, times, next)) {
            throw new IllegalStateException("last edit evaluates as false");
        }
        token = last;
        return true;
    }

    public Action getAction() {
        return new Action() {
            public void execute(VimEmulator vim) {
                Platform p = vim.getPlatform();
                p.beginChange();
                p.setRepaint(false);
                token.getAction().execute(vim);
                p.setRepaint(true);
                p.endChange();
            }
        };
    }

    public Space getSpace(Token next) {
        return Space.MODEL;
    }
}
