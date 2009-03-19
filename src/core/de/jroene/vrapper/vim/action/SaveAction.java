package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.VimEmulator;

public class SaveAction implements Action {

    public void execute(VimEmulator vim) {
        vim.getPlatform().save();
    }

}
