package de.jroene.vrapper.vim.commandline;

import java.util.Iterator;

import de.jroene.vrapper.vim.VimEmulator;

public interface Evaluator {

    boolean evaluate(VimEmulator vim, Iterator<String> command);
}
