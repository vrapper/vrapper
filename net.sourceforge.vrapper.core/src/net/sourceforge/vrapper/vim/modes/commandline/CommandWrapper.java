package net.sourceforge.vrapper.vim.modes.commandline;


import java.util.Queue;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class CommandWrapper implements Evaluator {

    private final Command action;

    public CommandWrapper(Command action) {
        super();
        this.action = action;
    }

    public Object evaluate(EditorAdaptor vim, Queue<String> command) {
        try {
            action.execute(vim);
        } catch (CommandExecutionException e) {
            VrapperLog.info(e.getMessage());
        }
        return null;
    }

}
