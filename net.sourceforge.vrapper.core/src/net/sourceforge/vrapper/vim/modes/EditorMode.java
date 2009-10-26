package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.KeyMapProvider;

public interface EditorMode {
    String getName();
    void enterMode(ModeSwitchHint... args);
    void leaveMode();
    boolean handleKey(KeyStroke stroke);
    KeyMap resolveKeyMap(KeyMapProvider provider);
}
