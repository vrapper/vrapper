package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class KeyMapper implements Evaluator {

    private static final Pattern pattern = Pattern.compile("<(.+)>");

    public Object evaluate(EditorAdaptor vim, Queue<String> command) {
        String lhs = command.poll();
        String rhs = command.poll();
        if (lhs != null && rhs != null) {
            NormalMode mode = (NormalMode) vim.getMode(NormalMode.NAME);
            mode.overrideMapping(
                    parseKeyStroke(lhs),
                    parseKeyStroke(rhs));
        }
        return null;
    }

    private KeyStroke parseKeyStroke(String s) {
        Matcher m = pattern.matcher(s);
        if (m.matches()) {
            String key = m.group(1).toLowerCase();
            if (key.startsWith("c-")) {
                return new SimpleKeyStroke(KeyStroke.CTRL, key.charAt(2));
            }
        }
        return new SimpleKeyStroke(0, s.charAt(0));
    }
}
