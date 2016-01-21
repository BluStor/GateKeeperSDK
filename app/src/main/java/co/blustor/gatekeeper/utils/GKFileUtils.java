package co.blustor.gatekeeper.utils;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public class GKFileUtils {
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
