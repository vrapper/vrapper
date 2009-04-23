package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.utils.Function;

/**
 * Accepts any keystroke and converts it to a value.
 *
 * @author Matthias Radig
 */
public class KeyStrokeConvertingState<T> implements State<T> {

    Function<T, KeyStroke> converter;

    public KeyStrokeConvertingState(Function<T, KeyStroke> converter) {
        super();
        this.converter = converter;
    }

    public Transition<T> press(KeyStroke key) {
        return new SimpleTransition<T>(converter.call(key));
    }

    public Iterable<KeyStroke> supportedKeys() {
        throw new UnsupportedOperationException("all keys supported");
    }

    public State<T> union(State<T> other) {
        throw new UnsupportedOperationException("no union possible");
    }

    public String stateIdentifier() {
        return getClass().getCanonicalName();
    }

}
