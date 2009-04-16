package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.VimEmulator;

/**
 * An action is the result of some command the user typed.
 *
 * @author Matthias Radig
 */
public interface Action {

    void execute(VimEmulator vim);
}
