package de.jroene.vrapper.vim.commandline;

import java.util.Iterator;

import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.token.Token;

public interface Evaluator {

    Token evaluate(VimEmulator vim, Iterator<String> command);
}
