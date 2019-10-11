package xyz.stupidwolf.ioc.util;

public class StringUtils {
    public static boolean isEmpty(String string) {
        return string == null || "".equals(string);
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }
}
