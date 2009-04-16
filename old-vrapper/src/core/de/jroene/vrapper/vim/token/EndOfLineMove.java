package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.VimEmulator;

/**
 * Move to the end of the current line.
 *
 * @author Matthias Radig
 */
public class EndOfLineMove extends AbstractMove implements Move {

    @Override
    protected int calculateTarget(VimEmulator vim, Token next) {
        return vim.getPlatform().getLineInformation().getEndOffset();
    }

    @Override
    public boolean isHorizontal() {
        return true;
    }


}
