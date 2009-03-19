package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

/**
 * Sets the active register to be used in following yank / delete / put actions.
 *
 * @author Matthias Radig
 */
public class UseRegister extends AbstractToken {

    private String registerName;
    private Token action;

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {

        if (registerName != null) {
            vim.getRegisterManager().setActiveRegister(registerName);
            if (action == null) {
                action = next;
                next = null;
            }
            return action.evaluate(vim, next);
        } else {
            if (next == null) {
                vim.toCharacterMode();
                return false;
            } else if (next instanceof KeyStrokeToken) {
                registerName = String.valueOf(((KeyStrokeToken) next).getPayload());
                return false;
            } else {
                throw new TokenException();
            }
        }
    }

    public Action getAction() {
        return action.getAction();
    }

    public Space getSpace() {
        if(action != null) {
            return action.getSpace();
        }
        return Space.MODEL;
    }

}
