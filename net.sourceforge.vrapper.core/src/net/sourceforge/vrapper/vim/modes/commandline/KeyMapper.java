package net.sourceforge.vrapper.vim.modes.commandline;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;

import java.util.Queue;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SimpleRemapping;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class KeyMapper implements Evaluator {

    final String[] keymaps;

    public KeyMapper(String... keymaps) {
        this.keymaps = keymaps;
    }

    public static class Map extends KeyMapper {

        private final boolean recursive;

        public Map(boolean recursive, String... keymaps) {
            super(keymaps);
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

    public static class Unmap extends KeyMapper {

        public Unmap(String... keymaps) {
            super(keymaps);
        }

        public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            if (!command.isEmpty()) {
                Iterable<KeyStroke> mapping = ConstructorWrappers.parseKeyStrokes(command.poll());
                for (String name : keymaps) {
                    KeyMap map = vim.getKeyMapProvider().getKeyMap(name);
                    map.removeMapping(mapping);
                }
            }
            return null;
        }
    }

    public static class Clear extends KeyMapper {

        public Clear(String... keymaps) {
            super(keymaps);
        }

        public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            for (String name : keymaps) {
                KeyMap map = vim.getKeyMapProvider().getKeyMap(name);
                map.clear();
            }
            return null;
        }
    }
}
