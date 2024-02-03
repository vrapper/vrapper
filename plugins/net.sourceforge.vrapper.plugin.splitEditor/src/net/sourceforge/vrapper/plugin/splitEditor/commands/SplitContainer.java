package net.sourceforge.vrapper.plugin.splitEditor.commands;

/**
 * Editor split option regarding shared area (MArea).
 */
public enum SplitContainer {
    /**
     * Make the new split in the shared area if the current editor is in the
     * shared area.
     */
    SHARED_AREA,
    /**
     * New split always goes to the top-level container leaving the shared
     * area untouched.
     */
    TOP_LEVEL
}
