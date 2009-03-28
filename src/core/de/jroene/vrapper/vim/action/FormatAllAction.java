package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;

public class FormatAllAction implements Action {

    public void execute(VimEmulator vim) {
        Platform p = vim.getPlatform();
        int line = p.getLineInformation().getNumber();
        vim.getPlatform().format(null);
        p.setPosition(VimUtils.getSOLAwarePositionAtLine(vim, line));
    }

}
