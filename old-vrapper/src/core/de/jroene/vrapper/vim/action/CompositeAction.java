package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.VimEmulator;

/**
 * Executes a number of actions sequentially.
 *
 * @author Matthias Radig
 */
public class CompositeAction extends TokenAndAction {

    Action[] actions;

    public CompositeAction(Action... actions) {
        this.actions = actions;
    }
    public void execute(VimEmulator vim) {
        for(Action action : actions) {
            action.execute(vim);
        }
    }

}
