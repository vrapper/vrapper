package net.sourceforge.vrapper.vim.modes;

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

    public static final String NO_KEYMAP = KeyMapResolver.class.getCanonicalName()+".NO_KEYMAP";
    /** Operator-pending keymap ID. */
    public static final String OMAP_NAME = KeyMapResolver.class.getCanonicalName()+".OMAP";

    private final State<String> initialState;
    private final String defaultValue;
    private State<String> currentState;
    private String lastValue;

    KeyMapResolver(State<String> initialState, String defaultValue) {
        this.initialState = initialState;
        this.currentState = initialState;
        this.defaultValue = defaultValue;
        this.lastValue = defaultValue;
    }

    public void press(KeyStroke key) {
        if (currentState != null) {
            Transition<String> trans = currentState.press(key);
            if (trans != null) {
                if (trans.getNextState() != null) {
                    currentState = trans.getNextState();
                }
                lastValue = getValue(trans);
            }
        }
    }

    private String getValue(Transition<String> trans) {
        String value = trans.getValue();
        return value != null ? value : defaultValue;
    }

    public String getKeyMapName() {
        return lastValue;
    }

    public void reset() {
        currentState = initialState;
        lastValue = defaultValue;
    }


}
