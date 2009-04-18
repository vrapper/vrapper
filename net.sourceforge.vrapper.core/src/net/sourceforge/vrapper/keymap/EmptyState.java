package net.sourceforge.vrapper.keymap;

import java.util.ArrayList;

public class EmptyState<T> implements State<T> {

    public Transition<T> press(KeyStroke key) {
        return null;
    }

    public Iterable<KeyStroke> supportedKeys() {
        return new ArrayList<KeyStroke>();
    }

    public State<T> union(State<T> other) {
        return other;
    }

}
