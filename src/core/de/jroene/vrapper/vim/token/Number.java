package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

/**
 * A number, used to multiply movement / edit actions.
 *
 * @author Matthias Radig
 */
public class Number extends AbstractToken implements Token {

    private final String number;
    private Number successor;
    private Repeatable token;
    private int multiplier;

    public Number(String number) {
        super();
        this.number = number;
        this.multiplier = 1;
    }

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        if (token != null) {
            return token.repeat(vim, evaluateNumber(), next);
        }
        if (next == null) {
            return false;
        }
        if (next instanceof Number) {
            if (successor == null) {
                successor = (Number)next;
            } else {
                return successor.evaluate(vim, next);
            }
            return false;
        }
        if (next instanceof Repeatable) {
            token = (Repeatable) next;
            return token.repeat(vim, evaluateNumber(), null);
        }
        throw new TokenException();
    }

    private int evaluateNumber() {
        return multiplier * Integer.parseInt(concatNumber());
    }

    String concatNumber() {
        if (successor == null) {
            return number;
        } else {
            return number + successor.concatNumber();
        }
    }

    public Action getAction() {
        return token.getAction();
    }

    public NumberMove asMove() {
        return new NumberMove(this);
    }

    public Space getSpace() {
        if (token != null) {
            return token.getSpace();
        }
        return Space.MODEL;
    }

    private static class NumberMove extends AbstractToken implements RepeatableMove {

        private final Number delegate;

        public NumberMove(Number delegate) {
            super();
            if(!(delegate.token == null || delegate.token instanceof Move)) {
                throw new IllegalArgumentException("delegate must be of type Move");
            }
            this.delegate = delegate;
        }

        public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
            delegate.multiplier = 1;
            return evaluate0(vim, next);
        }

        public int getTarget() {
            return ((Move)delegate.token).getTarget();
        }

        public boolean isHorizontal() {
            return ((Move)delegate.token).isHorizontal();
        }

        public Action getAction() {
            return delegate.getAction();
        }

        public boolean repeat(VimEmulator vim, int times, Token next)
        throws TokenException {
            delegate.multiplier = times;
            return evaluate0(vim, next);
        }

        private boolean evaluate0(VimEmulator vim, Token next)
        throws TokenException {
            if(next instanceof Move) {
                return delegate.evaluate(vim, next);
            }
            if(next == null) {
                return false;
            }
            throw new TokenException();
        }

        public Space getSpace() {
            return delegate.getSpace();
        }

        public boolean includesTarget() {
            return ((Move)delegate.token).includesTarget();
        }
    }

}
