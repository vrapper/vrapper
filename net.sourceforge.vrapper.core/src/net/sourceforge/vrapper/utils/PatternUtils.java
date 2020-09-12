package net.sourceforge.vrapper.utils;

import java.util.regex.Pattern;

/** Util class related to patterns (eg. regex) */
public class PatternUtils {
    
    /**
     * Convert a shell pattern (containing for example wildcards *) to a regular expression.<br>
     * Source Vim: src/fileio.c : file_pat_to_reg_pat()<br>
     * <br>
     * If this method throw a PatternSyntaxException, then there is a problem to fix in the callee, here.
     * 
     * @param shellPattern The shell pattern
     * @return A regex pattern
     */
    public static Pattern shellPatternToRegex(final String shellPattern) {
        final String regex = "^.*" + shellPatternToRegexString(shellPattern) + ".*$";
        return Pattern.compile(regex);
    }
    
    /**
     * Convert a shell pattern (containing for example wildcards *) to a regular expression.<br>
     * Source Vim: src/fileio.c : file_pat_to_reg_pat()<br>
     * <br>
     * Some unclear Vim behaviours are kept:
     * <ul>
     * <li>^ and $ are not escaped</li>
     * </ul>
     * While others are changed:
     * <ul>
     * <li>{a,b} is replaced by \{a,b\} and not by \(a|b\) (Vim behaviour)</li>
     * </ul>
     * 
     * @param shellPattern The shell pattern
     * @return A regex pattern
     */
    public static String shellPatternToRegexString(final String shellPattern) {        
        String escapedPattern = shellPattern
            .replaceAll("\\*{2,}", "*")
            .replace("\\", "\\\\")            
            .replace("|", "\\|")
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace('?', '.')
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("{", "\\{")
            .replace("}", "\\}");
        
        return escapedPattern;
    }
    
    /** Private constructor to prevent instanciation since it is an Utils class */
    private PatternUtils() {}

}
