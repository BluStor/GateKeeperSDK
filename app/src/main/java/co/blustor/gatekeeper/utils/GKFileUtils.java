package co.blustor.gatekeeper.utils;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * GKFileUtils is a functional, static class intended for common operations
 * involving generic file attributes.
 */
public class GKFileUtils {
    /**
     * Join an array of {@link String} objects using the '/' path separator.
     *
     * @param paths the array of {@link String} objects to be joined
     * @return the array of {@code paths} delimited by '/'
     */
    public static String joinPath(String... paths) {
        ArrayList<String> list = nonblankPathSegments(paths);
        return GKStringUtils.join(list.toArray(new String[list.size()]), "/").replace("//", "/");
    }

    @NonNull
    private static ArrayList<String> nonblankPathSegments(Object[] paths) {
        ArrayList<String> list = new ArrayList<>();
        for (Object path : paths) {
            if (path != null && !path.equals("")) {
                list.add((String) path);
            }
        }
        return list;
    }
}
