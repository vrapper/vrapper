package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;

/**
 * Move to the line above.
 *
 * @author Matthias Radig
 */
public class UpMove extends AbstractRepeatableVerticalMove {

    @Override
    public int calculateTarget(VimEmulator vim, int times, Token next) {
        Platform p = vim.getPlatform();
        LineInformation currLine = p.getLineInformation();
        int number = currLine.getNumber()-times;
        return VimUtils.getPositionAtLine(vim, number);
    }

}
