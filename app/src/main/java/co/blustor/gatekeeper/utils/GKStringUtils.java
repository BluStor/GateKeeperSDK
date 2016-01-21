package co.blustor.gatekeeper.utils;

public class GKStringUtils {
    public static String join(String[] strings, String separator) {
        if (strings.length == 0) return "";

        StringBuilder sb = new StringBuilder();
        sb.append(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            sb.append(separator);
            sb.append(strings[i]);
        }
        return sb.toString();
    }
}
