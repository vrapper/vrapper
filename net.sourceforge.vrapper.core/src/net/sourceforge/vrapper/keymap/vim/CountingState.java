package net.sourceforge.vrapper.keymap.vim;


import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.binding;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.vrapper.keymap.ConvertingTransition;
import net.sourceforge.vrapper.keymap.HashMapState;
import net.sourceforge.vrapper.keymap.KeyBinding;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.keymap.UnionState;
import net.sourceforge.vrapper.vim.commands.Counted;


public class CountingState<T extends Counted<T>> implements State<T> {

    public static<T extends Counted<T>> State<T> wrap(State<T> wrapped) {
        List<KeyBinding<T>> bindings = new ArrayList<KeyBinding<T>>();
        for (char chr = '1'; chr <= '9'; chr++) {
            State<T> state = new CountingState<T>(Character.getNumericValue(chr), wrapped);
            bindings.add(binding(key(chr), transition(state)));
        }
        return new HashMapState<T>(bindings).union(wrapped);
    }

    private final int value;
    private final State<T> wrappedState;

    private CountingState(int value, State<T> wrappedState) {
        this.value = value;
        this.wrappedState = wrappedState;
    }

    public Transition<T> press(KeyStroke key) {
        if (key.getCharacter() >= '0' && key.getCharacter() <= '9') {
            int keyValue = Character.getNumericValue(key.getCharacter());
            return new SimpleTransition<T>(new CountingState<T>(10*value + keyValue, wrappedState));
        }
        Transition<T> transition = wrappedState.press(key);
        if (transition != null) {
            @SuppressWarnings("unchecked") // no covariance in java :-(
            State<Counted<T>> nextState = (State<Counted<T>>) transition.getNextState();
            return new ConvertingTransition<T, Counted<T>>(new ApplyCount<T>(value), transition.getValue(), nextState);
        }
        return null;
    }

    public Iterable<KeyStroke> supportedKeys() {
        Set<KeyStroke> result = new HashSet<KeyStroke>();
        for (char chr = '0'; chr <= '9'; chr++) {
            result.add(key(chr));
        }
        for (KeyStroke stroke: wrappedState.supportedKeys()) {
            result.add(stroke);
        }
        return result;

    }

	public State<T> union(State<T> other) {
        if (other instanceof CountingState<?>) {
            CountingState<T> otherCS = (CountingState<T>) other;
            if (otherCS.value == value) {
                return new CountingState<T>(value, wrappedState.union(otherCS.wrappedState));
            }
        }
        //		VrapperPlugin.info("Strange state union: " + this + " and " + other);
        return new UnionState<T>(this, other);
    }

}
