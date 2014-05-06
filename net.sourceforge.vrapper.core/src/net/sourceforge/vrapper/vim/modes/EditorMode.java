package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public interface EditorMode {
    /** Mode name - must be static. */
    String getName();
    void enterMode(ModeSwitchHint... hints) throws CommandExecutionException;
    void leaveMode(ModeSwitchHint... hints) throws CommandExecutionException;
    /** Display name. Can be dynamic, so it is always called after {@link #enterMode(ModeSwitchHint...)}. */
    String getDisplayName();
    boolean handleKey(KeyStroke stroke);
    KeyMap resolveKeyMap(KeyMapProvider provider);
    void addKeyToMapBuffer(KeyStroke stroke);
    void cleanMapBuffer(boolean mappingSucceeded);
}
