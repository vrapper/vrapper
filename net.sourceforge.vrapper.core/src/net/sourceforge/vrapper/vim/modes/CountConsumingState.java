/**
 *
 */
package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;

/**
 * Wraps a state. Number keys are mapped to a transition to the state itself.
 * Other keystrokes are given to the wrapped state.
 *
 * @author Matthias Radig
 */
final class CountConsumingState<T> implements State<T> {
    private final State<T> state;

    CountConsumingState(State<T> state) {
        this.state = state;
    }

    public Transition<T> press(KeyStroke key) {
        char c = key.getCharacter();
        if ('0' <= c && c <= '9') {
            return new SimpleTransition<T>(this);
        }
        return state.press(key);
    }

    public State<T> union(State<T> other) {
        throw new UnsupportedOperationException();
    }
}