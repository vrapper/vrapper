package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.test.VimTestCase;

public class WordMoveTest extends VimTestCase {

    public void testNextBegin() {
        platform.setBuffer("aaaa bbbb\n  cccc dddd\n");
        assertMovement("w", 5);
        assertMovement("w", 12);
        platform.setPosition(0);
        assertMovement("2w", 12);
        assertMovement("2w", platform.getBuffer().length());
        assertMovement("434w", platform.getBuffer().length());
        assertMovement("w", platform.getBuffer().length());
    }
}
