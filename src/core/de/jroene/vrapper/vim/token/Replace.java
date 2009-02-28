package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;
import de.jroene.vrapper.vim.action.Action;

/**
 * Replaces one or more characters with a specified character.
 *
 * @author Matthias Radig
 */
public class Replace extends AbstractToken implements Repeatable {

    private String character;
    private int times;

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        return repeat(vim, 1, next);
    }

    public boolean repeat(VimEmulator vim, int times, Token next) throws TokenException {
        if(next instanceof KeyStrokeToken) {
            character = ((KeyStrokeToken)next).getPayload();
            this.times = times;
            Platform p = vim.getPlatform();
            int position = p.getPosition();
            LineInformation line = p.getLineInformation();
            if (position + times - 1 < line.getEndOffset()) {
                return true;
            }
            throw new TokenException();
        }
        if(next == null) {
            vim.toCharacterNormalMode();
            return false;
        }
        throw new TokenException();
    }

    public Action getAction() {
        return new ReplaceAction();
    }

    private class ReplaceAction implements Action {

        public void execute(VimEmulator vim) {
            Platform p = vim.getPlatform();
            int position = p.getPosition();
            StringBuilder s = new StringBuilder();
            if (VimUtils.isNewLine(character)) {
                s.append(character);
            } else {
                for(int i = 0; i < times; i++) {
                    s.append(character);
                }
            }
            p.replace(position, times, s.toString());
            p.setPosition(position + times - 1);
        }

    }

    public Space getSpace() {
        return Space.MODEL;
    }

    @Override
    public boolean isOperator() {
        return true;
    }

}
