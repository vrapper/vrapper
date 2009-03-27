package de.jroene.vrapper.vim.commandline;

import java.util.Iterator;

import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.token.Token;

public class KeyMapper implements Evaluator {

    public Token evaluate(VimEmulator vim, Iterator<String> command) {
        String lhs = command.next();
        String rhs = command.next();
        if (lhs != null && rhs != null) {
            vim.getNormalMode().overrideMapping(lhs.charAt(0), rhs.charAt(0));
        }
        return null;
    }

}
