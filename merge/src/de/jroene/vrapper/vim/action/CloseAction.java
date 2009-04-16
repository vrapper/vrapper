package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.VimEmulator;

public class CloseAction implements Action {

    private final boolean force;

    public CloseAction(boolean force) {
        super();
        this.force = force;
    }

    public void execute(VimEmulator vim) {
        vim.getPlatform().close(force);
    }

}
