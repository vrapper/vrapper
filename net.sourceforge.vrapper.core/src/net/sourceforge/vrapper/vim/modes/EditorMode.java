package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.KeyMapProvider;

public interface EditorMode {
    String getName();
    void enterMode(ModeSwitchHint... hints);
    void leaveMode(ModeSwitchHint... hints);
    boolean handleKey(KeyStroke stroke);
    KeyMap resolveKeyMap(KeyMapProvider provider);
}
