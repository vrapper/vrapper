package net.sourceforge.vrapper.keymap;

import java.util.HashMap;
import java.util.Iterator;

import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;

/**
 * Maps collections of keystrokes to another collection of keystrokes.
 *
 * @author Matthias Radig
 */
public class KeyMap {

    /**
     * Group of keys which have a default binding in ANY mode, for example Control-[ for Escape.
     * These can be overriden with a mapping if really needed.
     */
    public static final HashMap<KeyStroke, KeyStroke> GLOBAL_MAP = new HashMap<KeyStroke, KeyStroke>();
    
    static {
        GLOBAL_MAP.put(ConstructorWrappers.ctrlKey('['), ConstructorWrappers.key(SpecialKey.ESC));
        GLOBAL_MAP.put(ConstructorWrappers.ctrlKey('C'), ConstructorWrappers.key(SpecialKey.ESC));
        GLOBAL_MAP.put(ConstructorWrappers.ctrlKey('H'), ConstructorWrappers.key(SpecialKey.BACKSPACE));
        GLOBAL_MAP.put(ConstructorWrappers.ctrlKey('I'), ConstructorWrappers.key(SpecialKey.TAB));
        GLOBAL_MAP.put(ConstructorWrappers.ctrlKey('J'), ConstructorWrappers.key(SpecialKey.RETURN));
        GLOBAL_MAP.put(ConstructorWrappers.ctrlKey('M'), ConstructorWrappers.key(SpecialKey.RETURN));
    }

    private KeyMapState root = new KeyMapState();

    /**
     * Remaps the given keystrokes.
     *
     * @param strokes
     *            the keystrokes to map
     * @param mapping
     *            the mapping
     */
    public void addMapping(Iterable<KeyStroke> strokes, Remapping mapping) {
        root.addMapping(strokes.iterator(), mapping);
    }

    /**
     * Removes the mapping of the given keystrokes if it exists.
     *
     * @param strokes
     *            the keystrokes to unmap
     */
    public void removeMapping(Iterable<KeyStroke> strokes) {
        root.removeMapping(strokes.iterator());
    }

    /**
     * Removes all mappings from this keymap.
     */
    public void clear() {
        root = new KeyMapState();
    }

    public Transition<Remapping> press(KeyStroke key) {
        return root.press(key);
    }

    private static class KeyMapState extends HashMapState<Remapping> {

        int transitions = 0;

        private void addMapping(Iterator<KeyStroke> strokes, Remapping mapping) {
            KeyStroke first = strokes.next();
            Transition<Remapping> trans = map.get(first);
            if (strokes.hasNext()) {
                if (trans != null) {
                    KeyMapState next = (KeyMapState) trans.getNextState();
                    if (next == null) {
                        next = new KeyMapState();
                        map.put(first, new SimpleTransition<Remapping>(trans.getValue(), next));
                    }
                    next.addMapping(strokes, mapping);
                } else {
                    KeyMapState next = new KeyMapState();
                    map.put(first, new SimpleTransition<Remapping>(next));
                    transitions += 1;
                    next.addMapping(strokes, mapping);
                }
            } else {
                if (trans != null) {
                    map.put(first, new SimpleTransition<Remapping>(mapping, trans.getNextState()));
                } else {
                    transitions += 1;
                    map.put(first, new SimpleTransition<Remapping>(mapping));
                }
            }
        }

        private void removeMapping(Iterator<KeyStroke> strokes) {
            KeyStroke first = strokes.next();
            Transition<Remapping> trans = map.get(first);
            if (strokes.hasNext()) {
                if (trans != null) {
                    KeyMapState next = (KeyMapState) trans.getNextState();
                    if (next != null) {
                        next.removeMapping(strokes);
                        if (next.transitions == 0) {
                            Remapping value = trans.getValue();
                            if (value != null) {
                                map.put(first, new SimpleTransition<Remapping>(value));
                            } else {
                                map.remove(first);
                                transitions -= 1;
                            }
                        }
                    } else {
                        map.remove(first);
                        transitions -= 1;
                    }
                }
            } else {
                if (trans != null) {
                    if (trans.getNextState() == null) {
                        map.remove(first);
                        transitions -= 1;
                    } else {
                        map.put(first, new SimpleTransition<Remapping>(trans.getNextState()));
                    }
                }
            }
        }
    }
}