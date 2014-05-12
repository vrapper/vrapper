/**
 *
 */
package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.keymap.UnionState;

/**
 * State which will cause count characters to be ignored.
 * Must be wrapped around another state.
 *
 * @author Matthias Radig
 */
public final class CountConsumingState<T> implements State<T> {
    private final State<T> state;
    private final T value;

    public CountConsumingState(T value, State<T> state) {
        this.state = state;
        this.value = value;
    }

    public Transition<T> press(KeyStroke key) {
        char c = key.getCharacter();
        if ('0' <= c && c <= '9') {
            return new SimpleTransition<T>(value, this);
        }
        return state.press(key);
    }

    public State<T> union(State<T> other) {
        return new UnionState<T>(this, other);
    }
}