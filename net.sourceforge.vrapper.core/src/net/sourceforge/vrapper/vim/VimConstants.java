package net.sourceforge.vrapper.vim;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.platform.SimpleConfiguration.NewLine;
import net.sourceforge.vrapper.utils.VimUtils;

/**
 * Holds some commonly used constants.
 *
 * @author Matthias Radig
 */
public class VimConstants {

    public static final Set<String> WHITESPACE = VimUtils.set(" ", "\t", "\n", "\r");
    public static final Set<String> NEWLINE = createNewlineSet();
    public static final Set<KeyStroke> PRINTABLE_KEYSTROKES = createPrintableKeyStrokes();
    public static final Set<KeyStroke> PRINTABLE_KEYSTROKES_WITH_NL = createPrintableKeyStrokesWithNL();
    public static final Set<SpecialKey> SPECIAL_KEYS_ALLOWED_FOR_INSERT = VimUtils.set(SpecialKey.BACKSPACE, SpecialKey.RETURN);
    //public static final String NEWLINE = System.getProperty("line.separator");
    public static final String SPACE = " ";
    public static final String WORD_CHAR_PATTERN = "[A-Za-z0-9_]";
    public static final String REGISTER_NEWLINE = System.getProperty("line.separator");
    public static final String BACKWARD_SEARCH_CHAR = "?";
    public static final String FORWARD_SEARCH_CHAR = "/";
    public static final String COMMAND_LINE_CHAR = ":";
    public static final String ESCAPE_CHAR = "\\";

    private static Set<String> createNewlineSet() {
        NewLine[] newlines = NewLine.values();
        String[] nl = new String[newlines.length];
        for (int i = 0; i < nl.length; i++) {
            nl[i] = newlines[i].nl;
        }
        return VimUtils.set(nl);
    }

    private static Set<KeyStroke> createPrintableKeyStrokes() {
        Set<KeyStroke> result = new HashSet<KeyStroke>();
        for(char c = ' '; c <= 0xFF; c++) {
            result.add(key(c));
        }
        return Collections.unmodifiableSet(result);
    }

    private static Set<KeyStroke> createPrintableKeyStrokesWithNL() {
        Set<KeyStroke> result = new HashSet<KeyStroke>(createPrintableKeyStrokes());
        result.add(key(SpecialKey.RETURN));
        return Collections.unmodifiableSet(result);
    }
}
