package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;

/**
 * Wraps around another state to resolve the appropriate keymap when using
 * named registers.
 *
 * @author Matthias Radig
 */
public final class RegisterKeymapState implements State<String> {

    private final State<String> registerKeyMapState;

    private final State<String> wrapped;

    public RegisterKeymapState(final String keymapName,
            State<String> wrapped) {
        super();
        this.wrapped = wrapped;
        registerKeyMapState = new State<String>() {
            public Transition<String> press(KeyStroke key) {
                return new SimpleTransition<String>(keymapName, RegisterKeymapState.this);
            }

            public Iterable<KeyStroke> supportedKeys() {
                return null;
            }

            public State<String> union(State<String> other) {
                return null;
            }
        };
    }

    public Transition<String> press(KeyStroke key) {
        if (key.getCharacter() == '"') {
            return new SimpleTransition<String>(KeyMapResolver.NO_KEYMAP, registerKeyMapState);
        }
        return wrapped.press(key);
    }

    public Iterable<KeyStroke> supportedKeys() { return null; }

    public State<String> union(State<String> other) { return null; }
}