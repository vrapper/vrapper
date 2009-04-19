package net.sourceforge.vrapper.keymap;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.utils.StringUtils;

/**
 * Wraps another state to support remapping of keystrokes.
 */

public class RemappingState<T> implements State<T>, Remapper<T> {

    State<Remapping> remappings;
    State<T> wrapped;

    private RemappingState(State<T> wrapped) {
        this(wrapped, new EmptyState<Remapping>());
    }

    private RemappingState(State<T> wrapped, State<Remapping> remappings) {
        this.wrapped = wrapped;
        this.remappings = remappings;
    }

    public Transition<T> press(KeyStroke key) {
        Transition<Remapping> transition = remappings.press(key);
        if (transition == null)
            return null;
        Transition<T> keysTransition = getIt(transition.getValue());
        State<Remapping> keysState = transition.getNextState();
        State<T> follow = null;
        if (keysState != null)
            follow = new RemappingState<T>(wrapped, keysState);
        if(follow != null) {
            if (keysTransition == null)
                return new SimpleTransition<T>(follow);
            @SuppressWarnings("unchecked")
            State<T> nextState = StateUtils.union(keysTransition.getNextState(), follow);
            return new SimpleTransition<T>(keysTransition.getValue(), nextState);
        }
        return keysTransition;
    }

    private Transition<T> getIt(Remapping remapping) {
        if (remapping == null)
            return null;
        State<T> nextState = remapping.isRecursive() ? this : wrapped;
        Transition<T> result = null;
        for (KeyStroke stroke : remapping.getKeyStrokes()) {
            if (nextState == null)
                return null;
            result = nextState.press(stroke);
            if (result == null)
                return null;
            nextState = result.getNextState();
        }
        return result;
    }

    public static<T> Remapper<T> wrap(State<T> wrapped) {
        return new RemappingState<T>(wrapped);
    }

    public Iterable<KeyStroke> supportedKeys() {
        return remappings.supportedKeys();
    }

    public State<T> union(State<T> other) {
        return new UnionState<T>(this, other);
    }

    public void addMapping(State<? extends Remapping> mappings) {
        @SuppressWarnings("unchecked")
        State<Remapping> castState = (State<Remapping>) mappings;
        remappings = castState.union(remappings);
    }

    public State<T> getState() {
        return new UnionState<T>(this, wrapped);
    }

}
