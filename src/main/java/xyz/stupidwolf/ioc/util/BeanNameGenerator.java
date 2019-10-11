package xyz.stupidwolf.ioc.util;

public class BeanNameGenerator {
    public static String toLowerCamelCase(String name) {
        if (name == null || name.length() <= 0) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
