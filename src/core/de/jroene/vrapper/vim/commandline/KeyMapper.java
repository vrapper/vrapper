package de.jroene.vrapper.vim.commandline;

import java.util.Iterator;

import de.jroene.vrapper.vim.VimEmulator;

public class KeyMapper implements Evaluator {

    public boolean evaluate(VimEmulator vim, Iterator<String> command) {
        String lhs = command.next();
        String rhs = command.next();
        if (lhs != null && rhs != null) {
            vim.getNormalMode().overrideMapping(lhs.charAt(0), rhs.charAt(0));
            return true;
        }
        return false;
    }

}
