package net.sourceforge.vrapper.vim.commands.motions;

public enum StickyColumnPolicy {
    /**
     * Never update sticky column nor touch EOL flag. For e.g. up / down motions.
     */
    NEVER,
    /**
     * Only update sticky column / EOL status if the cursor actually moved.
     * Used for left and right motions, especially if they stop at the end of the line.
     */
    ON_CHANGE,
    /**
     * Force the sticky column to the current column and reset the EOL flag.
     * Used for e.g. the '0', '^' and '+' motions.
     */
    RESET_EOL,
    /**
     * Set the "stick to EOL" flag, thus dynamically updating the sticky column.
     * Used for '$'.
     */
    TO_EOL;
}
