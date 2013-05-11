package net.sourceforge.vrapper.vim;

import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;

/**
 * Classes implementing this can receive a hint to
 *  change modes. This is actually more than just 
 *  a hint right now, but... whatever.
 *  
 * @author dhleong
 *
 */
public interface ModeChangeHintReceiver {

    void changeModeSafely(String name, ModeSwitchHint... args);
}
