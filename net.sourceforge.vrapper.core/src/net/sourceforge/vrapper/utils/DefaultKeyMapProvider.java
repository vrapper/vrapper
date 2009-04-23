package net.sourceforge.vrapper.utils;

import java.util.HashMap;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.platform.KeyMapProvider;

/**
 * Uses a {@link HashMap} to store different keymaps. {@link KeyMap} instances
 * are created lazily when they are looked up for the first time.
 *
 * @author Matthias Radig
 */
public class DefaultKeyMapProvider implements KeyMapProvider {

    private final HashMap<String, KeyMap> keymaps = new HashMap<String, KeyMap>();

    public KeyMap getKeyMap(String id) {
        if (!keymaps.containsKey(id)) {
            keymaps.put(id, new KeyMap());
        }
        return keymaps.get(id);
    }

}
