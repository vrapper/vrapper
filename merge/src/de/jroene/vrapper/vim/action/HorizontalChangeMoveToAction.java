package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.VimEmulator;

/**
 * Like {@link MoveToAction}, but updates the horizontal position after the move.
 *
 * @author Matthias Radig
 */
public class HorizontalChangeMoveToAction extends MoveToAction {

    public HorizontalChangeMoveToAction(int index) {
        super(index);
    }

    @Override
    public void execute(VimEmulator vim) {
        super.execute(vim);
        vim.updateHorizontalPosition();
    }

}
