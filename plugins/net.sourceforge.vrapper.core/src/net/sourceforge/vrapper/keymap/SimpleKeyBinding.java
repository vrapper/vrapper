package net.sourceforge.vrapper.keymap;

public class SimpleKeyBinding<T> implements KeyBinding<T> {

    private final Transition<T> transition;
    private final KeyStroke key;

    public SimpleKeyBinding(KeyStroke key, Transition<T> transition) {
        this.key = key;
        this.transition = transition;
    }

    public KeyStroke getKeyPress() {
        return key;
    }

    public Transition<T> getTransition() {
        return transition;
    }

}
