package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyMapInfo;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;

/**
 * Resolves the appropriate keymap for different input sequences.
 * Intended to be used by (command based) modes for resolving keymaps.
 *
 * @author Matthias Radig
 */
public final class KeyMapResolver {

    /** Operator-pending keymap ID. */
    public static final String OMAP_NAME = KeyMapResolver.class.getCanonicalName()+".OMAP";

    private final State<KeyMapInfo> initialState;
    private final KeyMapInfo initialKeyMap;
    private State<KeyMapInfo> currentState;
    private KeyMapInfo activeKeyMap;

    KeyMapResolver(State<KeyMapInfo> initialState, String startKeyMap) {
        this.initialState = initialState;
        this.currentState = initialState;
        this.initialKeyMap = new KeyMapInfo(startKeyMap, "init");
        this.activeKeyMap = initialKeyMap;
    }

    public void storeKey(KeyStroke key) {
        if (currentState == null) {
            activeKeyMap = null;
        } else {
            Transition<KeyMapInfo> trans = currentState.press(key);
            if (trans == null) {
                currentState = null;
                activeKeyMap = null;
            } else {
                currentState = trans.getNextState();
                activeKeyMap = trans.getValue();
            }
        }
    }

    public String getKeyMapName(KeyStroke stroke) {
        if (activeKeyMap == null || currentState == null) {
            return null;
        } else {
            Transition<KeyMapInfo> nextTrans = currentState.press(stroke);
            KeyMapInfo nextKeyMap = (nextTrans != null ? nextTrans.getValue() : null);
            if (nextKeyMap == null) {
                // Keystroke is unknown or we are in the middle of a command or something.
                // Use active keymap, its probably a remap.
                return activeKeyMap.getKeyMapName();
            } else if (nextKeyMap != null && ! nextKeyMap.getKeyGroupId().equals(activeKeyMap.getKeyGroupId())) {
                // Check that we aren't in the same key group: e.g. once a count group was started,
                // a following digit cannot initiate a remapping.
                return activeKeyMap.getKeyMapName();
            } else {
                return null;
            }
        }
    }

    public void reset() {
        currentState = initialState;
        activeKeyMap = initialKeyMap;
    }


}
