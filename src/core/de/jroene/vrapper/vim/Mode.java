package de.jroene.vrapper.vim;

/**
 * Represents a mode (e.g. normal mode) of {@link VimEmulator}.
 * There is always one mode which is considered active. The active mode is
 * responsible for handling input events.
 *
 * @author Matthias Radig
 */
public interface Mode {

    /**
     * Handles an input event.
     * @param e the actual event.
     * @return whether the keystroke should be passed to the underlying editor or not.
     */
    boolean type(VimInputEvent e);
}
