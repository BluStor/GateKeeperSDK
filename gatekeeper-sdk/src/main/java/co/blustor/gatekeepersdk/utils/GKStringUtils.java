package co.blustor.gatekeepersdk.utils;

/**
 * GKStringUtils is a functional, static class intended for common operations
 * involving {@code String} objects.
 */
public class GKStringUtils {
    /**
     * Join an array of {@link String} objects with a {@link String} separator.
     *
     * @param strings   the array of {@link String} objects to be joined
     * @param separator the {@link String} object used to delimit the {@code strings}
     * @return the array of {@code strings} delimited by the {@code separator}
     * @since 0.5.0
     */
    public static String join(String[] strings, String separator) {
        if (strings.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            sb.append(separator);
            sb.append(strings[i]);
        }
        return sb.toString();
    }
}
