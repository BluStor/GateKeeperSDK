package co.blustor.gatekeepersdk.utils;

import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.blustor.gatekeepersdk.data.GKFile;

/**
 * GKFileUtils is a functional, static class intended for common operations
 * involving generic file attributes.
 */
public class GKFileUtils {
    /**
     * Base path of the card
     */
    public static final String ROOT = "/data";
    /**
     * Base path for license storage
     */
    public static final String LICENSE_ROOT = "/license";

    private static final String DIRECTORY_GROUP = "([-d])";
    private static final String PERMISSIONS_GROUP = "\\S+";
    private static final String LINKS_GROUP = "\\s+\\S+";
    private static final String USER_GROUP = "\\s+\\S+";
    private static final String GROUP_GROUP = "\\s+\\S+";
    private static final String SIZE_GROUP = "\\s+(\\d+)";
    private static final String MONTH_GROUP = "\\s+\\S+";
    private static final String DAY_GROUP = "\\s+\\S+";
    private static final String YEAR_GROUP = "\\s+\\S+";
    private static final String NAME_GROUP = "\\s+(.*)";
    /**
     * Regex pattern for the return values of files
     */
    public static final Pattern FILE_PATTERN = Pattern.compile(
            DIRECTORY_GROUP + PERMISSIONS_GROUP +
                    LINKS_GROUP + USER_GROUP + GROUP_GROUP +
                    SIZE_GROUP + MONTH_GROUP + DAY_GROUP +
                    YEAR_GROUP + NAME_GROUP + "$");

    /**
     * Regex pattern for parsing data files by line
     */
    public static final String DATA_LINE_PATTERN = "(.*)(\r\n|\n)";

    /**
     * @param fileData the data returned from a LIST command
     * @return a {@code GKFile} built from the fileData
     * @since 0.15.0
     */
    public static GKFile parseFile(String fileData) {
        Matcher fileMatcher = GKFileUtils.FILE_PATTERN.matcher(fileData);
        if (fileMatcher.find()) {
            String typeString = fileMatcher.group(1);
            int size = Integer.parseInt(fileMatcher.group(2));
            String name = fileMatcher.group(3);
            GKFile.Type type = typeString.equals("d") ? GKFile.Type.DIRECTORY : GKFile.Type.FILE;
            return new GKFile(name, type, size);
        }

        return null;
    }

    /**
     * Join an array of {@link String} objects using the '/' path separator.
     *
     * @param paths the array of {@link String} objects to be joined
     * @return the array of {@code paths} delimited by '/'
     */
    public static String joinPath(String... paths) {
        ArrayList<String> list = nonblankPathSegments(paths);
        return GKStringUtils.join(list.toArray(new String[list.size()]), "/").replaceAll("/\\/+/", "/");
    }

    /**
     * Parse path into an ArrayList using the '/' path separator.
     *
     * @param path the string to be split into the ArrayList
     * @return the array of path segments
     */
    public static ArrayList<String> parsePath(String path) {
        return nonblankPathSegments(path.split("/"));
    }

    /**
     * Read a file and return as a String
     *
     * @param path      the filepath to append the extension to
     * @param extension the extension to be appended
     * @return the string representing the filename with extension
     * @since 0.11.0
     */
    public static String addExtension(String path, String extension) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        if (extension == null || extension.isEmpty()) {
            return path;
        }

        return path + "." + extension;
    }

    /**
     * Read a file and return as a String
     *
     * @param file the file to read
     * @return the string representing the contents of the file
     * @throws IOException when reading the file fails
     * @since 0.11.0
     */
    public static String readFile(File file) throws IOException {
        Reader reader = new FileReader(file);
        BufferedReader br = new BufferedReader(reader);
        try {
            StringBuilder sb = new StringBuilder();
            for (; ; ) {
                String line = br.readLine();
                if (line == null) {
                    return sb.toString();
                } else {
                    sb.append(line);
                }
                sb.append(System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
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
