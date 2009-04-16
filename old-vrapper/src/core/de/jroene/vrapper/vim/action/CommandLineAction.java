package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.VimEmulator;

/**
 * Switches to command line mode.
 *
 * @author Matthias Radig
 */
public class CommandLineAction extends TokenAndAction {

    public void execute(VimEmulator vim) {
        vim.toCommandLineMode();
    }

}
