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
        platform.setBuffer("public static void main(String[] args) { test(); blub(a1, a2, a3); }");
        platform.setPosition(0);
        assertMovement("w", 7);
        assertMovement("2w", 19);
        assertMovement("w", 23);
        assertMovement("w", 24);
        assertMovement("2w", 33);
        assertMovement("w", 37);
    }

    public void testNextEnd() {
        platform.setBuffer("aaaa bbbb\n  cccc dddd\n");
        assertMovement("e", 3);
        assertMovement("e", 8);
        platform.setPosition(0);
        assertMovement("2e", 8);
        assertMovement("2e", platform.getBuffer().length()-2);
        assertMovement("434e", platform.getBuffer().length());
        assertMovement("e", platform.getBuffer().length());
        platform.setBuffer("public static void main(String[] args) { test(); blub(a1, a2, a3); }");
        platform.setPosition(0);
        assertMovement("e", 5);
        assertMovement("2e", 17);
        assertMovement("e", 22);
        assertMovement("e", 23);
        assertMovement("2e", 31);
        assertMovement("e", 36);
    }

    public void testLastBegin() {
        platform.setBuffer("aaaa bbbb\n  cccc dddd\n");
        platform.setPosition(8);
        assertMovement("b", 5);
        assertMovement("b", 0);
        platform.setPosition(platform.getBuffer().length());
        assertMovement("2b", 12);
        assertMovement("2b", 0);
        assertMovement("434b", 0);
        assertMovement("b", 0);
        platform.setBuffer("public static void main(String[] args) { test(); blub(a1, a2, a3); }");
        platform.setPosition(36);
        assertMovement("b", 33);
        assertMovement("2b", 24);
        assertMovement("b", 23);
        assertMovement("2b", 14);
        assertMovement("b",  7);
        assertMovement("b",  0);
        assertMovement("b",  0);
    }

}
