/**
 *
 */
package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.KeyMapInfo;
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
public final class CountConsumingKeyMapState implements State<KeyMapInfo> {
    private final State<KeyMapInfo> state;
    private final KeyMapInfo value;

    public CountConsumingKeyMapState(String keyMapName, String countGroup, State<KeyMapInfo> state) {
        this.state = state;
        this.value = new KeyMapInfo(keyMapName, countGroup);
    }

    public Transition<KeyMapInfo> press(KeyStroke key) {
        char c = key.getCharacter();
        if ('0' <= c && c <= '9') {
            return new SimpleTransition<KeyMapInfo>(value, this);
        }
        return state.press(key);
    }

    public State<KeyMapInfo> union(State<KeyMapInfo> other) {
        return new UnionState<KeyMapInfo>(this, other);
    }
}