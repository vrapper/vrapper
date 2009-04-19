package net.sourceforge.vrapper.keymap;

import java.util.Arrays;

/**
 * Basic implementation of key re-mapping.
 *
 * @author Matthias Radig
 */
public class SimpleRemapping implements Remapping {

    private final Iterable<KeyStroke> keystrokes;

    public SimpleRemapping(KeyStroke mapped) {
        this(Arrays.asList(mapped));
    }

    public SimpleRemapping(Iterable<KeyStroke> mapped) {
        this.keystrokes = mapped;
    }

    public Iterable<KeyStroke> getKeyStrokes() {
        return keystrokes;
    }

    public boolean isRecursive() {
        return false;
    }

}
