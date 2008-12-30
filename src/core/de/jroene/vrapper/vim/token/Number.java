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

    public Number(String number) {
        super();
        this.number = number;
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

    public int evaluateNumber() {
        return Integer.parseInt(concatNumber());
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

    public Space getSpace() {
        if (token != null) {
            return token.getSpace();
        }
        return Space.MODEL;
    }
}
