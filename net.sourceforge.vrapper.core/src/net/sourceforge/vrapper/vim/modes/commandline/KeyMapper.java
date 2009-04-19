package net.sourceforge.vrapper.vim.modes.commandline;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;

import java.util.Queue;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class KeyMapper implements Evaluator {

    public Object evaluate(EditorAdaptor vim, Queue<String> command) {
        String lhs = command.poll();
        String rhs = command.poll();
        if (lhs != null && rhs != null) {
            NormalMode mode = (NormalMode) vim.getMode(NormalMode.NAME);
            mode.overrideMapping(parseKeyStrokes(lhs), parseKeyStrokes(rhs));
        }
        return null;
    }
}
