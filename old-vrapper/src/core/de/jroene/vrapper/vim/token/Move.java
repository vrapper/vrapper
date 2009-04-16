package de.jroene.vrapper.vim.token;

/**
 * A move to an offset in the text.
 * 
 * @author Matthias Radig
 */
public interface Move extends Token {

    /**
     * @return the target of the move.
     */
    int getTarget();

    /**
     * @return whether this move changes the horizontal position in a line.
     */
    boolean isHorizontal();

    /**
     * @return whether edit tokens should include the target offset in their
     *         area of effect.
     */
    boolean includesTarget();
}
