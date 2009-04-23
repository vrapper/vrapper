package net.sourceforge.vrapper.vim.modes.commandline;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;

import java.util.Queue;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.SimpleRemapping;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class KeyMapper implements Evaluator {

    private final String[] keymaps;
    private final boolean recursive;

    public KeyMapper(boolean recursive, String... keymaps) {
        super();
        this.keymaps = keymaps;
        this.recursive = recursive;
    }

    public Object evaluate(EditorAdaptor vim, Queue<String> command) {
        String lhs = command.poll();
        String rhs = command.poll();
        if (lhs != null && rhs != null) {
            for (String name : keymaps) {
                KeyMap map = vim.getKeyMapProvider().getKeyMap(name);
                map.addMapping(
                        parseKeyStrokes(lhs),
                        new SimpleRemapping(parseKeyStrokes(rhs), recursive));
            }
        }
        return null;
    }
}
