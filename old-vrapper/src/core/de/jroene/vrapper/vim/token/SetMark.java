package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

/**
 * Sets a mark.
 *
 * @author Matthias Radig
 */
public class SetMark extends AbstractToken {

    private String name;

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        if (next == null) {
            vim.toCharacterMode();
            return false;
        }
        if (next instanceof KeyStrokeToken) {
            name = ((KeyStrokeToken)next).getPayload();
            return true;
        }
        throw new TokenException();
    }

    public Action getAction() {
        return new Action() {
            public void execute(VimEmulator vim) {
                vim.getPlatform().setMark(name);
            }
        };
    }

    public Space getSpace(Token next) {
        return Space.MODEL;
    }


}
