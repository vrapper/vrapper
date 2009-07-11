package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.modes.KeyMapResolver;

/**
 * Provides States defining key mappings for platform specific commands.
 *
 * @author Matthias Radig
 */
public interface PlatformSpecificStateProvider {

    /**
     * @param modeName
     *            name of a mode
     * @return a {@link State} containing (platform specific) key bindings which
     *         will be integrated into the calling mode.
     */
    State<Command> getState(String modeName);

    /**
     * @param name
     *            name of a mode
     * @return a {@link State} containing key map bindings which will be
     *         integrated into the {@link KeyMapResolver} of the calling mode.
     */
    State<String> getKeyMaps(String name);
}
