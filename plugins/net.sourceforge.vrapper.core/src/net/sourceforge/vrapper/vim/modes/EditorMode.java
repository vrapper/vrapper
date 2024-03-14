package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public interface EditorMode {
    /** Mode name - must be static. */
    String getName();
    void enterMode(ModeSwitchHint... hints) throws CommandExecutionException;
    void leaveMode(ModeSwitchHint... hints) throws CommandExecutionException;
    /** Display name. Can be dynamic, so it is always called after {@link #enterMode(ModeSwitchHint...)}. */
    String getDisplayName();
    boolean handleKey(KeyStroke stroke);
    String resolveKeyMap(KeyStroke stroke);
    void addKeyToMapBuffer(KeyStroke stroke);
    void cleanMapBuffer(boolean mappingSucceeded);
    /**
     * Whether a failed multi-character remap match should be retried without the first character.
     * Command modes best return <code>false</code> whereas modes with a continuous stream of input
     * (Insert, Command line) can keep the default of <code>true</code>
     */
    boolean isRemapBacktracking();
}
