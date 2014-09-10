package net.sourceforge.vrapper.utils;

import java.util.Arrays;

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

}
