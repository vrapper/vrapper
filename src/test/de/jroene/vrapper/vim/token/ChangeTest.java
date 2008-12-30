package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.test.VimTestCase;
import de.jroene.vrapper.vim.VimInputEvent;

public class ChangeTest extends VimTestCase {

    public void testDelete() {
        platform.setBuffer("aaaabbbbccccdddd");
        assertEdit("cl", 0, "aaabbbbccccdddd");
        type(VimInputEvent.ESCAPE);
        assertEdit("3cl", 0, "bbbbccccdddd");
        type(VimInputEvent.ESCAPE);
        assertEdit("4cl", 0, "ccccdddd");
        type(VimInputEvent.ESCAPE);
        assertEdit("c4l", 0, "dddd");
        type(VimInputEvent.ESCAPE);
        assertEdit("2c2l", 0, "");
        type(VimInputEvent.ESCAPE);

        platform.setBuffer("aaaabbbb");
        assertEdit("ch", 0, "aaaabbbb");
        type(VimInputEvent.ESCAPE);
        assertEdit("4ch", 0, "aaaabbbb");
        type(VimInputEvent.ESCAPE);
        platform.setBuffer("aaaabbbb");
        platform.setPosition(7);
        assertEdit("ch", 7, "aaaabbb");
        type(VimInputEvent.ESCAPE);
        assertEdit("4ch", 3, "aab");
        type(VimInputEvent.ESCAPE);
    }

    public void testLineDelete() {
        platform.setBuffer("aaaa\nbbbb\ncccc\ndddd\n");
        platform.setPosition(2);
        assertEdit("cc", 0, "\nbbbb\ncccc\ndddd\n");
        type(VimInputEvent.ESCAPE);
        assertEdit("2cc", 0, "\ncccc\ndddd\n");
        type(VimInputEvent.ESCAPE);
        platform.setBuffer("bbbb\ncccc\ndddd\n");
        platform.setPosition(2);
        assertEdit("cj", 0, "\ndddd\n");
        type(VimInputEvent.ESCAPE);
        platform.setBuffer("bbbb\ncccc\ndddd\n");
        platform.setPosition(2);
        assertEdit("2cj", 0, "\n");
        type(VimInputEvent.ESCAPE);
        platform.setBuffer("aaaa\nbbbb\ncccc\ndddd\n");
        platform.setPosition(17);
        assertEdit("2ck", 5, "aaaa\n\n");
        type(VimInputEvent.ESCAPE);
    }

    public void testWordDelete() {
        platform.setBuffer("word word\n another word");
        platform.setPosition(5);
        assertEdit("cw", 5, "word \n another word");
        type(VimInputEvent.ESCAPE);
        platform.setBuffer("word word\n another word");
        platform.setPosition(0);
        assertEdit("2cw", 0, "\n another word");
        type(VimInputEvent.ESCAPE);
        platform.setPosition(0);
        platform.setBuffer("word word\n another word");
        assertEdit("c2w", 0, "\n another word");
        type(VimInputEvent.ESCAPE);
    }
}
