package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.VimEmulator;

/**
 * Repeat the last character search.
 *
 * @author Matthias Radig
 */
public class RepeatFindMove extends AbstractRepeatableHorizontalMove {

    private final boolean reverse;

    public RepeatFindMove(boolean reverse) {
        super();
        this.reverse = reverse;
    }

    @Override
    public int calculateTarget(VimEmulator vim, int times, Token next) {
        FindMove original = vim.getRegisterManager().getLastCharSearch();
        if(reverse) {
            original = original.backwards();
        }
        return original.calculateTarget(vim, times, next);
    }
}
