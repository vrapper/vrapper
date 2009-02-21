package de.jroene.vrapper.vim;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds some commonly used constants.
 *
 * @author Matthias Radig
 */
public class VimConstants {

    public static final Set<String> WHITESPACE = set(" ", "\t", "\n", "\r");
    public static final String NEWLINE = System.getProperty("line.separator");
    public static final String SPACE = " ";
    public static final String WORD_CHAR_PATTERN = "[A-Za-z0-9_,]";

    private static final <T> Set<T> set(T... content) {
        return Collections.unmodifiableSet(new HashSet<T>(Arrays.asList(content)));
    }

}
