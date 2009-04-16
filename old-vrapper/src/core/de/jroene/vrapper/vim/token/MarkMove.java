package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Mark;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;

/**
 * Moves to a mark.
 *
 * @author Matthias Radig
 */
public class MarkMove extends AbstractMove {

    private final boolean lineWise;
    private String name;

    public MarkMove(boolean lineWise) {
        super();
        this.lineWise = lineWise;
    }

    @Override
    protected int calculateTarget(VimEmulator vim, Token next) {
        Mark m = vim.getPlatform().getMark(name);
        if (m == null) {
            return -1;
        }
        return lineWise ? VimUtils.getFirstNonWhiteSpaceOffset(vim, m.getLine()) : m.getPosition();
    }


    @Override
    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        if(next == null) {
            vim.toCharacterMode();
            return false;
        }
        if(next instanceof KeyStrokeToken) {
            name = String.valueOf(((KeyStrokeToken)next).getPayload());
            return super.evaluate(vim, next);
        }
        throw new TokenException();
    }

    @Override
    public boolean isHorizontal() {
        return !lineWise;
    }

}
