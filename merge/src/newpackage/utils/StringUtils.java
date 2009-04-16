package newpackage.utils;

public class StringUtils {
	public static String multiply(String str, int count) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++)
			builder.append(str);
		return builder.toString();
	}
}
