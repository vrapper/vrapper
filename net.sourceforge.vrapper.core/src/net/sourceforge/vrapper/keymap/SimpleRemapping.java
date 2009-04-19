package net.sourceforge.vrapper.keymap;

import java.util.Arrays;

/**
 * Basic implementation of key re-mapping.
 *
 * @author Matthias Radig
 */
public class SimpleRemapping implements Remapping {

    private final KeyStroke key;
    private final Iterable<KeyStroke> keystrokes;

    public SimpleRemapping(KeyStroke key, KeyStroke mapped) {
        this(key, Arrays.asList(mapped));
    }

    public SimpleRemapping(KeyStroke key, Iterable<KeyStroke> mapped) {
        super();
        this.key = key;
        this.keystrokes = mapped;
    }

    public Iterable<KeyStroke> getKeyStrokes() {
        return keystrokes;
    }

    public boolean isRecursive() {
        return false;
    }

    public Transition<Remapping> press(KeyStroke key) {
        return new SimpleTransition<Remapping>(this);
    }

    public Iterable<KeyStroke> supportedKeys() {
        return Arrays.asList(key);
    }

    public State<Remapping> union(State<Remapping> other) {
        throw new UnsupportedOperationException("union not supported");
    }

}
