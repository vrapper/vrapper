package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimConstants;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimInputEvent;
import de.jroene.vrapper.vim.action.Action;

/**
 * Represents an actual character. Used for selecting registers.
 *
 * @author Matthias Radig
 */
public class KeyStrokeToken implements Token {

    public static final KeyStrokeToken NOT_A_CHARACTER = new KeyStrokeToken(' ') {
        @Override
        public char getPayload() throws TokenException {
            throw new TokenException();
        }
    };
    private final char payload;

    private KeyStrokeToken(char payload) {
        super();
        this.payload = payload;
    }
    public static KeyStrokeToken from(VimInputEvent e) {
        if(e instanceof VimInputEvent.Character) {
            return new KeyStrokeToken(((VimInputEvent.Character)e).getCharacter());
        }
        if (VimInputEvent.RETURN.equals(e)) {
            return new KeyStrokeToken(VimConstants.NEWLINE.charAt(0));
        }
        return NOT_A_CHARACTER;
    }

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        throw new TokenException();
    }

    public char getPayload() throws TokenException {
        return payload;
    }

    public Action getAction() {
        return null;
    }

    @Override
    public Token clone() {
        try {
            return (Token) super.clone();
        } catch (CloneNotSupportedException e) {
            // does not happen
            return null;
        }
    }
    public Space getSpace() {
        return null;
    }

    public boolean isOperator() {
        return false;
    }

}
