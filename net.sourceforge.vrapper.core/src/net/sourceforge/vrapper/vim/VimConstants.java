package net.sourceforge.vrapper.vim;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.vrapper.keymap.KeyStroke;

/**
 * Holds some commonly used constants.
 *
 * @author Matthias Radig
 */
public class VimConstants {

    public static final Set<String> WHITESPACE = set(" ", "\t", "\n", "\r");
    public static final Set<String> NEWLINE = set("\r", "\n", "\r\n");
    public static final Set<KeyStroke> PRINTABLE_KEYSTROKES = createPrintableKeyStrokes();
    //public static final String NEWLINE = System.getProperty("line.separator");
    public static final String SPACE = " ";
    public static final String WORD_CHAR_PATTERN = "[A-Za-z0-9_]";
    public static final String REGISTER_NEWLINE = System.getProperty("line.separator");
    public static final String BACKWARD_SEARCH_CHAR = "?";
    public static final String FORWARD_SEARCH_CHAR = "/";
    public static final String COMMAND_LINE_CHAR = ":";
    public static final String ESCAPE_CHAR = "\\";

    private static final <T> Set<T> set(T... content) {
        return Collections.unmodifiableSet(new HashSet<T>(Arrays.asList(content)));
    }

    private static Set<KeyStroke> createPrintableKeyStrokes() {
        Set<KeyStroke> result = new HashSet<KeyStroke>();
        for(char c = ' '; c <= 0xFF; c++) {
            result.add(key(c));
        }
        return Collections.unmodifiableSet(result);
    }
}
