package de.jroene.vrapper.vim.commandline;


import java.util.Queue;

import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

public class ActionWrapper implements Evaluator {

    private final Action action;

    public ActionWrapper(Action action) {
        super();
        this.action = action;
    }

    public Object evaluate(VimEmulator vim, Queue<String> command) {
        action.execute(vim);
        return null;
    }

}
