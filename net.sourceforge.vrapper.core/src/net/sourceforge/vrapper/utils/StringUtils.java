package net.sourceforge.vrapper.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtils {
    public static String multiply(String str, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(str);
        }
        return builder.toString();
    }

    public static String join(String separator, Object[] items) {
        return join(separator, Arrays.asList(items));
    }

    public static String join(String separator, Iterable<?> items) {
        StringBuilder builder = new StringBuilder();
        String sep = "";
        for (Object item : items) {
            builder.append(sep);
            builder.append(item);
            sep = separator;
        }
        return builder.toString();
    }
    
    public static int countNewlines(String lines) {
        return lines.length() - lines.replace("\n", "").length();
    }

    public static boolean containsUppercase(String keyword) {
        for (int i = 0; i < keyword.length(); i++) {
            if (Character.isUpperCase(keyword.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String repr(String str) {
        if (str == null)
            return "null";

        StringBuilder result = new StringBuilder("\"");
        for (int i = 0; i < str.length(); i++) {
            char chr = str.charAt(i);
            switch (chr) {
                case '\0': result.append("\\0"); break;
                case '\t': result.append("\\t"); break;
                case '\r': result.append("\\r"); break;
                case '\n': result.append("\\n"); break;
                case '\"': result.append("\\\""); break;
                case '\\': result.append("\\\\"); break;
                default:
                    result.append(isPrintableASCII(chr) ? chr : String.format("\\u%04x", chr));
            }
        }
        return result.append('"').toString();
    }

    public static boolean isPrintableASCII(char chr) {
        return ' ' <= chr && chr <= '\u007e';
    }

    /**
     * Utility method to calculate the visual offset of each character in a string starting from the
     * beginning of the string and taking the variable width of a tab in account. The newline chars
     * are ignored, but one extra offset is added to know where the line ends visually.
     * @return an array which is maxIndex + 1 long and which contains the visual offset of each
     * character offset in the string.
     */
    public static int[] calculateVisualOffsets(String contents, int maxIndex, int tabstop) {
        if (maxIndex > contents.length()) {
            maxIndex = contents.length();
        }
        int[] result = new int[maxIndex + 1];
        int nextTabstopOff = 0;
        int visualOffset = 0;
        int i = 0;
        while (i < maxIndex) {
            result[i] = visualOffset;
            if (visualOffset % tabstop == 0) {
                nextTabstopOff += tabstop;
            }
            if (contents.charAt(i) == '\t') {
                visualOffset = nextTabstopOff;
            } else if ( ! Character.isHighSurrogate(contents.charAt(i))) {
                visualOffset++;
            }
            i++;
        }
        result[i] = visualOffset;
        return result;
    }

    public static ExplodedPattern explodePattern(String pattern) {
        List<String> parts = new ArrayList<String>();
        for (int i = 0; i < pattern.length(); i++) {
            char currentChar = pattern.charAt(i);
            if (currentChar == '\\' && i + 1 == pattern.length()) {
                throw new IllegalArgumentException("Unmatched backslash found at end of pattern!");
            } else if (currentChar == '\\') {
                char nextChar = pattern.charAt(i + 1);
                // Escape codes with three chars.
                if (nextChar == '%' && i + 2 == pattern.length()) {
                    throw new IllegalArgumentException("Backslash-% without extra token found at "
                            + "end of pattern!");
                } else if (nextChar == '%') {
                    parts.add(pattern.substring(i, i + 3));
                    i += 2;
                } else {
                    parts.add(pattern.substring(i, i + 2));
                    i++;
                }
            } else if (Character.isHighSurrogate(currentChar) && i + 1 == pattern.length()) {
                throw new IllegalArgumentException("Unmatched surrogate found at end of pattern!");
            } else if (Character.isHighSurrogate(currentChar)) {
                parts.add(pattern.substring(i, i + 1));
                i++;
            } else {
                parts.add(pattern.substring(i, i + 1));
            }
        }
        return new ExplodedPattern(parts);
    }

}
