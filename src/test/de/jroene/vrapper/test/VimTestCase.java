package de.jroene.vrapper.test;

import junit.framework.TestCase;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimInputEvent;
import de.jroene.vrapper.vim.VimInputEvent.Character;

/**
 * A prototype {@link TestCase} for testing various tokens
 *
 * @author Matthias Radig
 */
public class VimTestCase extends TestCase {

    protected TestPlatform platform;
    protected VimEmulator vim;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        platform = new TestPlatform();
        vim = new VimEmulator(platform);
    }

    /**
     * Wraps every character of the given string into a {@link Character} and
     * passes it to the {@link VimEmulator}.
     */
    protected void type(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            VimInputEvent e = new VimInputEvent.Character(c);
            boolean doit = vim.type(e);
            if (doit) {
                platform.replace(platform.getPosition(), 0, String.valueOf(c),
                        false);
            }
        }
    }

    /**
     * Passes the given {@link VimInputEvent}s to the {@link VimEmulator}.
     * @param events
     */
    protected void type(VimInputEvent... events) {
        for (VimInputEvent e : events) {
            boolean doit = vim.type(e);
            if (doit && e instanceof VimInputEvent.Character) {
                platform.replace(platform.getPosition(), 0,
                        String .valueOf(((VimInputEvent.Character) e).getCharacter()),
                        false);
            }
        }
    }

    protected void assertMovement(String string, int i) {
        type(string);
        assertEquals(i, platform.getPosition());
    }

    protected void assertEdit(String edit, int pos, String bufferContent) {
        type(edit);
        assertEquals(bufferContent, platform.getBuffer());
        assertEquals(pos, platform.getPosition());
    }

}
