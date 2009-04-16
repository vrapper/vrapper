package de.jroene.vrapper.vim.token;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.jroene.vrapper.test.VimTestCase;
import de.jroene.vrapper.vim.Search;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.WordSearchMove;

public class WordSearchActionTest extends VimTestCase {

    public void testGetSearch() throws SecurityException,
    NoSuchMethodException, IllegalArgumentException,
    IllegalAccessException, InvocationTargetException {
        platform.setBuffer("");
        assertKeyWord(0, "");
        platform.setBuffer(" ");
        assertKeyWord(0, "");
        platform.setBuffer("test hallo 123 ha_12");
        assertKeyWord(0, "test");
        assertKeyWord(1, "test");
        assertKeyWord(3, "test");
        assertKeyWord(4, "hallo");
        assertKeyWord(5, "hallo");
        assertKeyWord(9, "hallo");
        assertKeyWord(10, "123");
        assertKeyWord(11, "123");
        assertKeyWord(12, "123");
        assertKeyWord(13, "123");
        assertKeyWord(14, "ha_12");
        assertKeyWord(15, "ha_12");
        assertKeyWord(19, "ha_12");
    }

    private void assertKeyWord(int offset, String string) throws IllegalArgumentException,
    IllegalAccessException, InvocationTargetException,
    SecurityException, NoSuchMethodException {
        platform.setPosition(offset);
        Method getSearch = WordSearchMove.class
        .getDeclaredMethod("getSearch", VimEmulator.class);
        getSearch.setAccessible(true);
        WordSearchMove a = new WordSearchMove(false);
        Search s = (Search) getSearch.invoke(a, vim);
        assertEquals(string, s.getKeyword());
    }
}
