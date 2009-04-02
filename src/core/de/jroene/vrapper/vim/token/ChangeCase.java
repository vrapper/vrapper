package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

/**
 * Changes the case of a char (upper-case to lower-case and vice versa)
 *
 * @author Johannes Weiß
 */
public class ChangeCase extends AbstractToken implements Repeatable {
    private int changes;

    public Action getAction() {
        return new ChangeCaseAction();
    }

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        return repeat(vim, 1, next);
    }

    public boolean repeat(VimEmulator vim, int times, Token next)
    throws TokenException {
        Platform p = vim.getPlatform();
        LineInformation l = p.getLineInformation();
        if(p.getPosition()+times > l.getEndOffset()) {
            changes = l.getEndOffset()-p.getPosition();
        } else {
            changes = times;
        }
        return true;
    }

    public Space getSpace(Token next) {
        return Space.MODEL;
    }

    @Override
    public boolean isOperator() {
        return true;
    }

    public class ChangeCaseAction implements Action {

        public void execute(VimEmulator vim) {
            for(int i = 0; i < changes; i++) {
                changeCase(vim);
            }
            vim.getPlatform().setUndoMark();
        }

        private void changeCase(VimEmulator vim) {
            Platform p = vim.getPlatform();

            char c = p.getText(p.getPosition(), 1).charAt(0);

            if(Character.isUpperCase(c)) {
                c = Character.toLowerCase(c);
            } else {
                c = Character.toUpperCase(c);
            }

            p.replace(p.getPosition(), 1, ""+c);
            p.setPosition(p.getPosition()+1);
        }
    }
}
