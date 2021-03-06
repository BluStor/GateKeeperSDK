package co.blustor.gatekeepersdk.data;

import java.util.ArrayList;

import co.blustor.gatekeepersdk.utils.GKFileUtils;

/**
 * A GKFile represents a file or path on a GateKeeper Card.
 */
public class GKFile {
    public static final String TAG = GKFile.class.getCanonicalName();
    /**
     * The absolute path to the entry on the GateKeeper Card.
     */
    protected String mCardPath;
    /**
     * The name of the file.
     */
    protected String mName;
    /**
     * The {@code Type} of the file.
     */
    protected Type mType;
    /**
     * The size of the file.
     */
    private int mFileSize;

    /**
     * Create a {@code GKFile} with the given {@code name} and {@code type}.
     *
     * @param name the {@code String} name of the file
     * @param type the {@code Type} of the file
     * @since 0.5.0
     */
    public GKFile(String name, Type type) {
        mName = name;
        mType = type;
    }

    /**
     * Create a {@code GKFile} with the given {@code name} and {@code type}.
     *
     * @param name the {@code String} name of the file
     * @param type the {@code Type} of the file
     * @param fileSize the {@code int} size of the file in bytes
     * @since 0.15.0
     */
    public GKFile(String name, Type type, int fileSize) {
        mName = name;
        mType = type;
        mFileSize = fileSize;
    }

    /**
     * Retrieve the name of the file.
     *
     * @return the name of the file
     * @since 0.5.0
     */
    public String getName() {
        return mName;
    }

    /**
     * Assign the name of the file.
     *
     * @param name the {@code String} name of the file
     * @since 0.5.0
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Retrieve the type of the file.
     *
     * @return the type of the file
     * @since 0.5.0
     */
    public Type getType() {
        return mType;
    }

    /**
     * Assign the type of the file.
     *
     * @param type the {@code Type} of the file
     * @since 0.5.0
     */
    public void setType(Type type) {
        mType = type;
    }

    /**
     * Retrieve whether the file refers to a directory or a file entry on the GateKeeper Card.
     *
     * @return {@code true} if the file refers to a directory on the GateKeeper Card or
     * {@code false} if the file refers to a file entry.
     * @since 0.5.0
     */
    public boolean isDirectory() {
        return mType == Type.DIRECTORY;
    }

    /**
     * Retrieve the file system extension (when present) of the file.
     *
     * @return the {@code String} extension of the file.
     * {@code NULL} if the file is a directory or does not have an extension.
     * @since 0.5.0
     */
    public String getExtension() {
        if (mType == Type.DIRECTORY) {
            return null;
        }
        String[] parts = mName.split("\\.");
        String ext = (parts.length > 1) ? parts[parts.length - 1] : null;
        return ext;
    }

    /**
     * Retrieve the filename without the extension (when present) of the file.
     *
     * @return the {@code String} name of the file without extension.
     * {@code NULL} if the file is a directory or does not have an extension.
     * @since 0.11.0
     */
    public String getFilenameBase() {
        if (mType == Type.DIRECTORY) {
            return null;
        }
        int extensionIndex = mName.lastIndexOf(".");
        if (extensionIndex == -1) {
            extensionIndex = mName.length();
        }
        return mName.substring(0, extensionIndex);
    }

    /**
     * Retrieve the absolute path to the file on the GateKeeper Card.
     *
     * @return the absolute {@code String} path to the file
     * @since 0.5.0
     */
    public String getCardPath() {
        return mCardPath;
    }

    /**
     * Assign the absolute path to the file on the GateKeeper Card.
     *
     * @param fullPath the absolute {@code String} path to the file
     * @since 0.5.0
     */
    public void setCardPath(String fullPath) {
        mCardPath = fullPath;
    }

    /**
     * Assign the absolute path to the file on the GateKeeper Card.
     *
     * @param parentPath the absolute {@code String} path to the parent of the file
     * @param fileName   the {@code String} name of the file
     * @since 0.5.0
     */
    public void setCardPath(String parentPath, String fileName) {
        setCardPath(GKFileUtils.joinPath(parentPath, fileName));
    }

    /**
     * Retrieve the parent directory
     *
     * @return the parent directory if present, null otherwise
     * @since 0.19.0
     */
    public String getParentCardPath() {
        if (getCardPath() == null) {
            return null;
        }

        ArrayList<String> parts = GKFileUtils.parsePath(getCardPath());
        if (parts.size() <= 1) {
            return null;
        }

        int newSize = parts.size() - 1;
        String[] parentParts = parts.subList(0, newSize).toArray(new String[newSize]);
        return "/" + GKFileUtils.joinPath(parentParts);
    }

    /**
     * Retrieve the size of the file as returned from the card.
     *
     * @return the {@code int} value of the file size
     * @since 0.15.0
     */
    public int getFileSize() {
        return mFileSize;
    }

    /**
     * The type of the entry on the GateKeeper Card.
     */
    public enum Type {
        FILE,
        DIRECTORY
    }
}
