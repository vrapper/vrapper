package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.VimEmulator;

/**
 * Sets the position to a specific index.
 *
 * @author Matthias Radig
 */
public class MoveToAction implements Action {

    private final int index;

    public MoveToAction(int index) {
        super();
        this.index = index;
    }
    public void execute(VimEmulator vim) {
        vim.getPlatform().setPosition(index);
    }

}
