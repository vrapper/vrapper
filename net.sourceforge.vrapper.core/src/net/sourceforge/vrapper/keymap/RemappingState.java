package net.sourceforge.vrapper.keymap;

import java.util.HashSet;

import net.sourceforge.vrapper.utils.Function;

/**
 * Wraps another state to support remapping of keystrokes.
 *
 * @author Matthias Radig
 */
public class RemappingState<T> extends ConvertingState<T, Remapping> implements Function<T, Remapping>{

    private final State<T> wrappedState;

    public RemappingState(State<T> wrapped) {
        super(null, new EmptyState<Remapping>());
        wrappedState = wrapped;
    }

    public T call(Remapping remapping) {
        State<T> state = remapping.isRecursive() ? this : wrappedState;
        for (KeyStroke stroke : remapping.getKeyStrokes()) {
            Transition<T> trans = state.press(stroke);
            if (trans == null) {
                return null;
            }
            state = trans.getNextState();
            if (state == null) {
                T value = trans.getValue();
                return value;
            }
        }
        return null;
    }

    public void addMapping(Remapping mapping) {
        super.addMapping(this, mapping);
    }

    @Override
    public Transition<T> press(KeyStroke key) {
        Transition<T> t = super.press(key);
        return t == null ? wrappedState.press(key) : t;
    }

    @Override
    public Iterable<KeyStroke> supportedKeys() {
        HashSet<KeyStroke> set = new HashSet<KeyStroke>();
        set.addAll(map.keySet());
        for (KeyStroke stroke : wrappedState.supportedKeys()) {
            set.add(stroke);
        }
        return set;
    }

}
