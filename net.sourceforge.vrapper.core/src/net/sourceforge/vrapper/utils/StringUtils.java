package net.sourceforge.vrapper.utils;


public class StringUtils {
	public static String multiply(String str, int count) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
            builder.append(str);
        }
		return builder.toString();
	}

    public static String join(String separator, Iterable<?> items) {
        StringBuilder builder = new StringBuilder();
        String sep = "";
        for (Object item: items) {
            builder.append(sep);
            builder.append(item);
            sep = separator;
        }
        return builder.toString();
    }

    public static boolean containsUppercase(String keyword) {
        for (int i = 0; i < keyword.length(); i++) {
            if (Character.isUpperCase(keyword.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
