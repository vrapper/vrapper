package net.sourceforge.vrapper.vim.modes.commandline;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SimpleRemapping;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

public abstract class KeyMapper implements Evaluator {

    private static final KeyStroke LEADER_KEY = new SimpleKeyStroke(SpecialKey.LEADER);

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
            String rhs = "";
            while( ! command.isEmpty()) {
                //restore spaces between extra parameters
                rhs += command.poll() + " ";
            }
            if (lhs != null && ! "".equals(rhs)) {
                boolean useRecursive = recursive;

                // Simple prefix maps are non-recursive, e.g. nmap n nzz - Vim detects this as well.
                if (recursive && rhs.startsWith(lhs)) {
                    useRecursive = false;
                    vim.getUserInterfaceService().setInfoMessage("Changing recursive remap '" + lhs
                            + "' to non-recursive.");
                }
                String mapLeader = vim.getConfiguration().get(Options.MAPLEADER);
                List<KeyStroke> leaderKeys = new ArrayList<KeyStroke>();
                for (KeyStroke keystroke : parseKeyStrokes(mapLeader)) {
                    leaderKeys.add(keystroke);
                }
                rhs = rhs.trim();
                Iterable<KeyStroke> lhsKeyStrokes = replaceLeader(parseKeyStrokes(lhs), leaderKeys);
                Iterable<KeyStroke> rhsKeyStrokes = replaceLeader(parseKeyStrokes(rhs), leaderKeys);

                for (String name : keymaps) {
                    KeyMap map = vim.getKeyMapProvider().getKeyMap(name);
                    map.addMapping( lhsKeyStrokes, new SimpleRemapping(rhsKeyStrokes, useRecursive));
                }
            }
            return null;
        }

        protected static Iterable<KeyStroke> replaceLeader(Iterable<KeyStroke> inputKeys,
                Collection<KeyStroke> leaderKeys) {

            List<KeyStroke> result = new ArrayList<KeyStroke>();
            for (KeyStroke keystroke : inputKeys) {
                if (keystroke != null) {
                    if (keystroke.equals(LEADER_KEY)) {
                        result.addAll(leaderKeys);
                    } else {
                        result.add(keystroke);
                    }
                }
            }
            return result;
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
