package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.VimEmulator;

public class SearchModeAction extends TokenAndAction {

    private final boolean backwards;

    public SearchModeAction(boolean backwards) {
        super();
        this.backwards = backwards;
    }

    public void execute(VimEmulator vim) {
        vim.toSearchMode(backwards);
    }

}
