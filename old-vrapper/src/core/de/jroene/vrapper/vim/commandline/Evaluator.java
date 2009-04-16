package de.jroene.vrapper.vim.commandline;


import java.util.Queue;

import de.jroene.vrapper.vim.VimEmulator;

public interface Evaluator {

    Object evaluate(VimEmulator vim, Queue<String> command);
}
