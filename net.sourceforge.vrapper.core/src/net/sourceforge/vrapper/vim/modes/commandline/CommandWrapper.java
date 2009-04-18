package net.sourceforge.vrapper.vim.modes.commandline;


import java.util.Queue;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;

public class CommandWrapper implements Evaluator {

    private final Command action;

    public CommandWrapper(Command action) {
        super();
        this.action = action;
    }

    public Object evaluate(EditorAdaptor vim, Queue<String> command) {
        action.execute(vim);
        return null;
    }

}
