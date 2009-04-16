package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.VimEmulator;

/**
 * Move to the right.
 *
 * @author Matthias Radig
 */
public class RightMove extends AbstractRepeatableHorizontalMove {

    @Override
    public int calculateTarget(VimEmulator vim, int offset, Token next) {
        int position = vim.getPlatform().getPosition();
        LineInformation line = vim.getPlatform().getLineInformationOfOffset(position);
        int newPos = position + offset;
        if (newPos > line.getEndOffset()) {
            return line.getEndOffset();
        }
        return newPos;
    }
}
