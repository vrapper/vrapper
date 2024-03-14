package net.sourceforge.vrapper.vim.modes;

import java.util.Collection;

import net.sourceforge.vrapper.keymap.KeyMapInfo;
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
public final class RegisterKeymapState implements State<KeyMapInfo> {

    private final State<KeyMapInfo> registerKeyMapState;

    private final State<KeyMapInfo> wrapped;

    public RegisterKeymapState(String keymapName,
            State<KeyMapInfo> wrapped) {
        super();
        this.wrapped = wrapped;
        final KeyMapInfo keyMapInfo = new KeyMapInfo(keymapName, "register");
        registerKeyMapState = new State<KeyMapInfo>() {
            public Transition<KeyMapInfo> press(KeyStroke key) {
                return new SimpleTransition<KeyMapInfo>(keyMapInfo, RegisterKeymapState.this);
            }

            public State<KeyMapInfo> union(State<KeyMapInfo> other) {
                return null;
            }
        };
    }

    public Transition<KeyMapInfo> press(KeyStroke key) {
        if (key.getCharacter() == '"') {
            return new SimpleTransition<KeyMapInfo>(null, registerKeyMapState);
        }
        return wrapped.press(key);
    }

    public Collection<KeyStroke> supportedKeys() { return null; }

    public State<KeyMapInfo> union(State<KeyMapInfo> other) { return null; }
}