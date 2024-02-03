package net.sourceforge.vrapper.vim.modes.commandline;


import java.util.Queue;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/** Wraps a {@link Command} in an {@link Evaluator} interface. Also does exception handling. */
public class CommandWrapper implements Evaluator {

    private final Command action;

    public CommandWrapper(Command action) {
        super();
        this.action = action;
    }
    
    public Command getCommand() {
        return action;
    }

    public Object evaluate(EditorAdaptor vim, Queue<String> command) {
        try {
            action.execute(vim);
        } catch (CommandExecutionException e) {
            vim.getUserInterfaceService().setErrorMessage(e.getMessage());
        }
        return null;
    }

}
