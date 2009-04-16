package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.VimEmulator;

public class VisualModeAction extends TokenAndAction {

    private final boolean lineWise;

    public VisualModeAction(boolean lineWise) {
        super();
        this.lineWise = lineWise;
    }

    public void execute(VimEmulator vim) {
        vim.toVisualMode(lineWise);
    }

}
