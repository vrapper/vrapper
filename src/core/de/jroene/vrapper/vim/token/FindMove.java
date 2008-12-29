package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;

/**
 * Searches for a character in the current line and jumps to its position.
 *
 * @author Matthias Radig
 */
public class FindMove extends AbstractRepeatableHorizontalMove {

    private String target;
    private final boolean backwards;
    private final boolean stopBeforeTarget;

    public FindMove(boolean backwards, boolean stopBeforeTarget) {
        this(null, backwards, stopBeforeTarget);
    }
    public FindMove(String target, boolean backwards, boolean stopBeforeTarget) {
        super();
        this.target = target;
        this.backwards = backwards;
        this.stopBeforeTarget = stopBeforeTarget;
    }

    public FindMove backwards() {
        return new FindMove(target, backwards, stopBeforeTarget);
    }

    @Override
    public int calculateTarget(VimEmulator vim, int times, Token next) {
        Platform p = vim.getPlatform();
        int index = p.getPosition();
        int oldPos = index;
        LineInformation line = p.getLineInformation();
        int end = backwards ? line.getBeginOffset() : line.getEndOffset();
        int modifier = backwards ? -1 : 1;
        for(int n = 0; n < times; n++) {
            while (index != end) {
                index += modifier;
                if(p.getText(index, 1).equals(target)) {
                    break;
                }
            }
        }
        if(!p.getText(index, 1).equals(target)) {
            return oldPos;
        }
        if(stopBeforeTarget && index != end) {
            index -= modifier;
        }
        return index;
    }

    @Override
    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        return repeat(vim, 1, next);
    }

    @Override
    public boolean repeat(VimEmulator vim, int times, Token next) throws TokenException {
        if(next instanceof KeyStrokeToken) {
            target = String.valueOf(((KeyStrokeToken)next).getPayload());
            vim.getVariables().setLastCharSearch(this);
            return super.repeat(vim, times, next);
        }
        if(next == null) {
            vim.toCharacterNormalMode();
            return false;
        }
        throw new TokenException();
    }

}
