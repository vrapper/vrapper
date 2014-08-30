package net.sourceforge.vrapper.vim;

import net.sourceforge.vrapper.vim.modes.EditorMode;

/**
 * Utility event methods so that the platform or extra plugins can be notified of Vrapper state
 * changes.
 * 
 * <p><b>NOTE</b>: The order in which the events are called is currently IMPLEMENTATION-SPECIFIC.
 * Implementers of this interface should not assume that the order will guaranteed. If this is
 * necessary for a plugin, please file an enhancement request.
 */
public interface VrapperEventListener {
    /**
     * Called just before a command is to be executed by the user. When internal commands are
     * called directly using their execute method, no event will be triggered.
     * <p>Useful to store the current Vrapper state.
     * @param mode EditorMode current mode implementation.
     */
    void commandAboutToExecute(EditorMode mode);

    /**
     * Called just after the user executed a command.
     * @param mode EditorMode current mode implementation.
     */
    void commandExecuted(EditorMode mode);

    /**
     * Called when a set of keys resulted in a command to be executed or when the keys weren't
     * recognized. Note that remaps are handled on a higher level.
     * @param mode EditorMode current mode implementation.
     * @param recognized whether the keys were recognized as a command or failed to do something.
     */
    void stateReset(EditorMode mode, boolean recognized);

    /**
     * Called just before switching to a new editor mode. Note that this may fail, causing another
     * "mode switched" event because we're switching back to currentMode.
     * @param currentMode EditorMode current mode implementation.
     * @param newMode EditorMode mode implementation of the mode we're switching to.
     */
    void modeAboutToSwitch(EditorMode currentMode, EditorMode newMode);

    /**
     * Called just after switching to a new editor mode. Note that this may be a different mode
     * compared to an earlier {@link #modeAboutToSwitch(EditorMode, EditorMode)} event when an
     * exception happened.
     * @param oldMode EditorMode mode implementation of the mode we're switched from.
     * @param currentMode EditorMode current mode implementation.
     */
    void modeSwitched(EditorMode oldMode, EditorMode currentMode);
    
    /**
     * Called after disabling Vrapper and just before enabling it.
     * @param enabled whether Vrapper is enabled or disabled.
     */
    void vrapperToggled(boolean enabled);
}
