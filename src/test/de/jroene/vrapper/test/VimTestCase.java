package de.jroene.vrapper.test;

import junit.framework.TestCase;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimInputEvent;
import de.jroene.vrapper.vim.VimInputEvent.Character;
import de.jroene.vrapper.vim.register.DefaultRegisterManager;

/**
 * A prototype {@link TestCase} for testing various tokens
 *
 * @author Matthias Radig
 */
public class VimTestCase extends TestCase {

    private static final char ESCAPE_CHAR = '-';
    protected TestPlatform platform;
    protected VimEmulator vim;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // prevent use of user-specific configuration
        System.setProperty("user.home", "");
        platform = new TestPlatform();
        vim = new VimEmulator(platform, new DefaultRegisterManager());
    }

    /**
     * Wraps every character of the given string into a {@link Character} and
     * passes it to the {@link VimEmulator}.
     */
    protected void type(String s) {
        for (int i = 0; i < s.length(); i++) {

            char c = s.charAt(i);
            VimInputEvent e;
            if (c == ESCAPE_CHAR) {
                e = VimInputEvent.ESCAPE;
            } else {
                e = new VimInputEvent.Character(c, false);
            }
            boolean doit = vim.type(e);
            if (doit) {
                platform.replace(platform.getPosition(), 0, String.valueOf(c));
                platform.setPosition(platform.getPosition()+1);
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
                        String .valueOf(((VimInputEvent.Character) e).getCharacter()));
                platform.setPosition(platform.getPosition()+1);
            }
        }
    }

    protected void assertMovement(String string, int i) {
        type(string);
        assertEquals(i, platform.getPosition());
    }

    protected void assertEdit(String edit, int pos, String bufferContent) {
        type(edit);
        String expected = bufferContent.replace("\r", "\\r").replace("\n", "\\n");
        String actual = platform.getBuffer().replace("\r", "\\r").replace("\n", "\\n");
        assertEquals(expected, actual);
        assertEquals(pos, platform.getPosition());
    }

}
