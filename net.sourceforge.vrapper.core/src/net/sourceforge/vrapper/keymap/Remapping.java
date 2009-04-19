package net.sourceforge.vrapper.keymap;


/**
 * Remapping of keys. May be recursive.
 *
 * @author Matthias Radig
 */
public interface Remapping {

    /**
     * @return the keystrokes mapped to the key
     */
    Iterable<KeyStroke> getKeyStrokes();

    /**
     * @return whether the mapping should be handled recursive
     */
    boolean isRecursive();
}
