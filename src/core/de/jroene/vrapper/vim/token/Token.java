package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

/**
 * An abstract representation of a keystroke the user made.
 * 
 * @author Matthias Radig
 */
public interface Token extends Cloneable {

    /**
     * Evaluates this token in combination with next.
     * 
     * @param vim
     *            the vim emulator.
     * @param next
     *            the {@link Token} following this one, or null if there is no
     *            such token yet.
     * @return whether this token can produce an action in its current state.
     * @throws TokenException
     *             if next cannot be combined with this instance.
     */
    boolean evaluate(VimEmulator vim, Token next) throws TokenException;

    /**
     * @return an appropiate action if and only if
     *         {@link #evaluate(VimEmulator, Token)} was called before and
     *         returned true.
     */
    Action getAction();

    Token clone();

    Space getSpace();
}
