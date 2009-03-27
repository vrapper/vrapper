package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;

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
    public int calculateTarget(VimEmulator vim, int line, Token next) {
        line -= 1;
        line = Math.max(line, 0);
        line = Math.min(line, vim.getPlatform().getNumberOfLines()-1);
        return VimUtils.getSOLAwarePositionAtLine(vim, line);
    }

    @Override
    public boolean isHorizontal() {
        return false;
    }

    @Override
    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        int line = defaultToLastLine ? vim.getPlatform().getNumberOfLines()-1 : 0;
        setTarget(calculateTarget(vim, line, next));
        return true;
    }

}
