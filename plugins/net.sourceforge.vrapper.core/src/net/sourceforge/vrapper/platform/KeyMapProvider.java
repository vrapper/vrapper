package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.keymap.KeyMap;

/**
 * Provides access to different keymaps.
 *
 * @author Matthias Radig
 */
public interface KeyMapProvider {

    /**
     * @param id
     *            name of a keymap
     * @return the keymap with the given name
     */
    KeyMap getKeyMap(String id);
}
