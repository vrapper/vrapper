package de.jroene.vrapper.vim.commandline;

import java.util.Iterator;

import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimInputEvent;
import de.jroene.vrapper.vim.token.Token;

public class KeyMapper implements Evaluator {

    public Token evaluate(VimEmulator vim, Iterator<String> command) {
        String lhs = command.next();
        String rhs = command.next();
        if (lhs != null && rhs != null) {
            vim.getNormalMode().overrideMapping(
                    new VimInputEvent.Character(lhs.charAt(0), false),
                    new VimInputEvent.Character(rhs.charAt(0), false));

        }
        return null;
    }

}
