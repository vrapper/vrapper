package net.sourceforge.vrapper.vim.modes.commandline;


import java.util.Queue;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public interface Evaluator {

    Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException;
}
