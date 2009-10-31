package net.sourceforge.vrapper.keymap.vim;

import java.util.Collection;
import java.util.Set;

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

    private final Function<T, KeyStroke> converter;
    private final Set<KeyStroke> supportedKeys;

    /**
     * Creates an instance which accepts all keys. Not compatible with any kind
     * of wrapping state. (e.g. {@link CountingState})
     */
    public KeyStrokeConvertingState(Function<T, KeyStroke> converter) {
        this(converter, null);
    }

    public KeyStrokeConvertingState(Function<T, KeyStroke> converter,
            Set<KeyStroke> supportedKeys) {
        super();
        this.converter = converter;
        this.supportedKeys = supportedKeys;
    }

    public Transition<T> press(KeyStroke key) {
        if (supportedKeys == null || supportedKeys.contains(key)) {
            return new SimpleTransition<T>(converter.call(key));
        }
        return null;
    }

    public Collection<KeyStroke> supportedKeys() {
        if (supportedKeys != null) {
            return supportedKeys;
        }
        throw new UnsupportedOperationException("all keys supported");
    }

    public State<T> union(State<T> other) {
        throw new UnsupportedOperationException("no union possible");
    }

    public String stateIdentifier() {
        return getClass().getCanonicalName();
    }

}
