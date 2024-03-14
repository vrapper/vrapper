package net.sourceforge.vrapper.keymap;

import java.util.Arrays;

/**
 * Basic implementation of key re-mapping.
 *
 * @author Matthias Radig
 */
public class SimpleRemapping implements Remapping {

    private final Iterable<KeyStroke> keystrokes;
    private final boolean recursive;

    public SimpleRemapping(KeyStroke mapped) {
        this(Arrays.asList(mapped), false);
    }

    public SimpleRemapping(Iterable<KeyStroke> keystrokes, boolean recursive) {
        super();
        this.keystrokes = keystrokes;
        this.recursive = recursive;
    }

    public Iterable<KeyStroke> getKeyStrokes() {
        return keystrokes;
    }

    public boolean isRecursive() {
        return recursive;
    }

    @Override
    public String toString() {
	    return String.format("SimpleRemapping(%s, %s)", keystrokes, recursive ? "recursive" : "non-recursive");
    }
}
