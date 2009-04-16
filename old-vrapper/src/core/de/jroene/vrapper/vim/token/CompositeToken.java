package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;
import de.jroene.vrapper.vim.action.CompositeAction;

/**
 * Combines two tokens into one. Differs from {@link CompositeAction} as the
 * second token is used as "argument" for the first.
 *
 * @author Matthias Radig
 */
public final class CompositeToken implements RepeatableMove {

    private final Token first;
    private final Token second;

    public CompositeToken(Token first, Token second) {
        super();
        this.first = first;
        this.second = second;
    }

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        return first.evaluate(vim, second);
    }

    public Action getAction() {
        return first.getAction();
    }

    public boolean repeat(VimEmulator vim, int times, Token next)
    throws TokenException {
        if(first instanceof Repeatable) {
            return ((Repeatable)first).repeat(vim, times, second);
        }
        throw new TokenException();
    }

    @Override
    public Token clone() {
        Token newFirst = null;
        Token newSecond = null;
        if (first != null) {
            newFirst = first.clone();
        }
        if (second != null) {
            newSecond = second.clone();
        }
        return new CompositeToken(newFirst, newSecond);
    }

    public Space getSpace(Token next) {
        return first.getSpace(next);
    }

    public boolean isOperator() {
        return first.isOperator();
    }

    public int getTarget() {
        return getMove().getTarget();
    }

    public boolean includesTarget() {
        return getMove().includesTarget();
    }

    public boolean isHorizontal() {
        return getMove().isHorizontal();
    }

    private Move getMove() {
        if (first instanceof Move) {
            return (Move)first;
        } else if (second instanceof Move) {
            return (Move) second;
        } else {
            throw new UnsupportedOperationException("this instance is not a Move");
        }
    }
}
