package net.sourceforge.vrapper.plugin.sneak.commands.utils;

import java.util.List;

import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Interface to abstract away the retrieval of Sneak's current state. This should help during
 * testing as the sneak mode might otherwise not be regisetered in the {@link EditorAdaptor}
 * instance.
 */
public interface SneakStateManager {
    /**
     * Read the current sneak state for this editor. This object is a clone of the actual state so
     * modifying it has no effect.
     * @param editorAdaptor {@link EditorAdaptor} for the current editor.
     * @return {@link SneakState}
     * @throws VrapperPlatformException when sneak hasn't been initialized yet.
     */
    public SneakState getSneakState(EditorAdaptor editorAdaptor);

    /**
     * Write the current sneak state.
     * @param editorAdaptor {@link EditorAdaptor} for the current editor.
     * @param backward boolean whether sneak moves toward the file start (necessary to know if
     *      highlighting needs to be calculated from scratch).
     * @param highlighting {@link List} of Object with current highligting state for later cleanup.
     * @param positions {@link List} of {@link Position}s to cache the possible target positions.
     */
    public void markSneakActive(EditorAdaptor editorAdaptor);

    /**
     * Stops the current sneak highlighting.
     * @param editorAdaptor {@link EditorAdaptor} for the current editor.
     */
    public void markSneakInactive(EditorAdaptor editorAdaptor);

    /**
     * Increases the event counters for detecting whether sneak should remain active.
     */
    public void increaseCounters(EditorAdaptor editorAdaptor);
}
