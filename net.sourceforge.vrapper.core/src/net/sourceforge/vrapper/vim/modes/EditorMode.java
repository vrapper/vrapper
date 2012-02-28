package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public interface EditorMode {
    String getDisplayName();
    String getName();
    void enterMode(ModeSwitchHint... hints) throws CommandExecutionException;
    void leaveMode(ModeSwitchHint... hints) throws CommandExecutionException;
    boolean handleKey(KeyStroke stroke);
    KeyMap resolveKeyMap(KeyMapProvider provider);
}
