package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.VimEmulator;

/**
 * Interface for all {@link Token}s which may be used in combination with a
 * {@link Number}.
 * 
 * @author Matthias Radig
 */
public interface Repeatable extends Token {

    /**
     * Like {@link Token#evaluate(VimEmulator, Token)}, but with a multiplier.
     * 
     * @param vim
     *            the vim emulator.
     * @param times
     *            the multiplier.
     * @param next
     *            the {@link Token} following this one, or null if there is no
     *            such token yet.
     * @return whether this token can produce an action in its current state.
     * @throws TokenException
     *             if next cannot be combined with this instance.
     */
    boolean repeat(VimEmulator vim, int times, Token next) throws TokenException;
}
