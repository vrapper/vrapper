package net.sourceforge.vrapper.core.tests.cases;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;
import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.Remapping;
import net.sourceforge.vrapper.keymap.SimpleRemapping;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;

/**
 * White-box tests for the {@link KeyMap} class.
 *
 */
public class KeyMapTests extends TestCase {

    private final KeyMap map = new KeyMap("test");
    private final Remapping mapping = new SimpleRemapping(key('h'));

    /**
     * Test if dynamically mapping and unmapping will build and traverse
     * the right state machine structure. The numbers present the number
     * of nodes at each level.
     */
    @Test
    public void testKeyMap() throws Exception {
        for (char i = 0; i < 0xFF; i++) {
            assertNull(map.press(key(i)));
        }
        assertMapping("abc", 1, 1, 1, 1);
        assertMapping("bbc", 2, 1, 1, 1);
        assertMapping("bbc", 2, 1, 1, 1);
        assertMapping("bac", 2, 2, 1, 1);
        assertMapping("bad", 2, 2, 2, 1);
        assertMapping("badd", 2, 2, 2, 1, 1);

        assertRemove("bad", "badd", 2, 2, 2, 1, 1);
        assertMapping("bad", 2, 2, 2, 1);
        assertMapping("badd", 2, 2, 2, 1, 1);

        assertRemove("b", "bac", 2, 2, 2, 1);
        assertRemove("bad", "badd", 2, 2, 2, 1, 1);
        assertRemove("badd", "bac", 2, 2, 1, 1);

        assertRemove("abc", "bbc", 1, 2, 1, 1);
    }

    private void assertRemove(String remove, String path, int... transitions) throws Exception {
        map.removeMapping(createStrokeList(remove));
        assertTransitionsOverPath(createStrokeList(path), transitions);
    }

    private void assertMapping(String string, int... transitions) throws Exception {
        List<KeyStroke> strokes = createStrokeList(string);
        map.addMapping(strokes, mapping);
        assertTransitionsOverPath(strokes, transitions);
    }

    private List<KeyStroke> createStrokeList(String string) {
        List<KeyStroke> strokes = new ArrayList<KeyStroke>();
        for(int i = 0; i < string.length(); i++) {
            strokes.add(key(string.charAt(i)));
        }
        return strokes;
    }

    private void assertTransitionsOverPath(List<KeyStroke> strokes,
            int... transitions) throws Exception {
        assertEquals("at root", transitions[0], getTransitions(su(map)));
        Transition<Remapping> next = map.press(strokes.get(0));
        for(int i = 1; i < strokes.size(); i++) {
            assertEquals("at state "+i, transitions[i], getTransitions(next.getNextState()));
            next = next.getNextState().press(strokes.get(i));
        }
        assertEquals(next.getValue(), mapping);
    }

    /*
     * Provides root access. haha!
     */
    @SuppressWarnings("unchecked")
    private State<Remapping> su(KeyMap map) throws Exception {
        Field root = KeyMap.class.getDeclaredField("root");
        root.setAccessible(true);
        return (State<Remapping>) root.get(map);
    }

    private int getTransitions(State<Remapping> state) throws Exception {
        Class<?> clazz = KeyMap.class.getDeclaredClasses()[0];
        Field f = clazz.getDeclaredField("transitions");
        f.setAccessible(true);
        return ((Integer)f.get(state)).intValue();
    }
}
