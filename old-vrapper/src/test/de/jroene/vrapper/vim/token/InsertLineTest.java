package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.test.VimTestCase;
import de.jroene.vrapper.vim.VimInputEvent;

public class InsertLineTest extends VimTestCase {
    
    public void testInsertLinePostCursor() {
        platform.setBuffer("000\n111\n222\n333");
        assertEdit("o", 4, "000\n\n111\n222\n333");
        type(VimInputEvent.ESCAPE);
    }

    public void testInsertLinePostCursorLastLine() {
        String content = "000\n111\n222\n333";
        platform.setBuffer(content);
        platform.setPosition(content.length());
        assertEdit("o", content.length()+1, content+"\n");
        type(VimInputEvent.ESCAPE);
    }
    
    public void testInsertLinePreCursorFirstLine() {
        platform.setBuffer("000\n111\n222\n333");
        assertEdit("O", 0, "\n000\n111\n222\n333");
        type(VimInputEvent.ESCAPE);
    }

}
