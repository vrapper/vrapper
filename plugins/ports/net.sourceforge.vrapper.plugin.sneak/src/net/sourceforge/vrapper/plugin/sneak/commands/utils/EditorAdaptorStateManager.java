package net.sourceforge.vrapper.plugin.sneak.commands.utils;

import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.plugin.sneak.modes.SneakInputMode;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.EditorMode;

/**
 * This implementation of {@link SneakStateManager} will extract the current sneak state from the
 * {@link SneakInputMode} in the current editor.
 */
public class EditorAdaptorStateManager implements SneakStateManager {

    /**
     * Retrieves the current sneak state.
     * @return {@link SneakState}
     * @throws VrapperPlatformException when sneak hasn't been initialized yet.
     */
    @Override
    public SneakState getSneakState(EditorAdaptor editorAdaptor) {
        SneakState result = null;
        EditorMode mode = editorAdaptor.getMode(SneakInputMode.NAME);
        // Note that the mode could be null
        if (mode instanceof SneakInputMode) {
            SneakInputMode sneakMode = (SneakInputMode) mode;
            result = sneakMode.getPreviousState();
            return result;
        } else {
            throw new VrapperPlatformException("Sneak input mode never got initialized");
        }
    }

    @Override
    public void markSneakActive(EditorAdaptor editorAdaptor) {

        SneakState sneakState = getSneakState(editorAdaptor);
        sneakState.keepSneakActive();
    }

    @Override
    public void markSneakInactive(EditorAdaptor editorAdaptor) {
        
        SneakState sneakState = getSneakState(editorAdaptor);
        sneakState.deactivateSneak(editorAdaptor.getHighlightingService());
    }

    @Override
    public void increaseCounters(EditorAdaptor editorAdaptor) {

        SneakState sneakState = getSneakState(editorAdaptor);

        long counter = sneakState.getGlobalEventCounter() + 1;
        sneakState.globalEventCounter = counter;
    }
}
