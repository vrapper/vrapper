package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.VimEmulator;

public class FormatAllAction implements Action {

    public void execute(VimEmulator vim) {
        vim.getPlatform().formatAll();
    }

}
