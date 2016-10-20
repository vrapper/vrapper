package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.vim.commands.motions.ContinueFindingMotion;

public interface Reversible<T> {
    /** returns T copy which will move in the other direction (eg. Motion repeated by
     * {@link ContinueFindingMotion}) compared to this one. This function should never return null.
     */
    T reverse();

    /**
     * While {@link #reverse()} can always return a copy for the reversed direction, this method
     * identifies if the current instance will move backward or not.
     */
    boolean isBackward();
}
