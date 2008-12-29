package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;

/**
 * Moves to the beginning of a specific line.
 *
 * @author Matthias Radig
 */
public class GotoMove extends AbstractRepeatableMove {

    private final boolean defaultToLastLine;

    public GotoMove(boolean defaultToLastLine) {
        super();
        this.defaultToLastLine = defaultToLastLine;
    }

    @Override
    public int calculateTarget(VimEmulator vim, int times, Token next) {
        times -= 1;
        times = Math.max(times, 0);
        times = Math.min(times, vim.getPlatform().getNumberOfLines()-1);
        return vim.getPlatform().getLineInformation(times).getBeginOffset();
    }

    @Override
    public boolean isHorizontal() {
        return false;
    }

    @Override
    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        Platform p = vim.getPlatform();
        setTarget(defaultToLastLine ? p.getLineInformation(p.getNumberOfLines()-1).getBeginOffset() : 0);
        return true;
    }

}
