package de.jroene.vrapper.vim;

import de.jroene.vrapper.test.VimTestCase;

public class SelectionTest extends VimTestCase {


    public void testCharWise() {
        platform.setBuffer("aaaabbbbccccdddd");
        assertDeleteSelected(0, 1, false, 0, "aaabbbbccccdddd");
        assertDeleteSelected(0, 3, false, 0, "bbbbccccdddd");
        assertDeleteSelected(0, 4, false, 0, "ccccdddd");
        assertDeleteSelected(0, 4, false, 0, "dddd");

        platform.setBuffer("aaaabbbb");
        assertDeleteSelected(0, 0, false, 0, "aaaabbbb");
        platform.setPosition(7);
        assertDeleteSelected(6, 1, false, 6, "aaaabbb");
        assertDeleteSelected(2, 4, false, 2, "aab");
    }

    public void testLineWise() {

    }

    private void assertDeleteSelected(int start, int length, boolean lineWise,
            int position, String bufferContent) {
        Selection s = new Selection(start, length, lineWise);
        platform.setSelection(s);
        assertEdit("d", position, bufferContent);
    }


}
