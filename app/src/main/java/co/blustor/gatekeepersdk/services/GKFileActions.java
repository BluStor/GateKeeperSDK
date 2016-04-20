package co.blustor.gatekeepersdk.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.blustor.gatekeepersdk.data.GKFile;
import co.blustor.gatekeepersdk.devices.GKCard;
import co.blustor.gatekeepersdk.devices.GKCard.Response;
import co.blustor.gatekeepersdk.utils.GKFileUtils;

/**
 * GKFileActions is a Service for handling file data with the GateKeeper Card.
 */
public class GKFileActions {
    public static final String TAG = GKFileActions.class.getSimpleName();
    private final GKCard mCard;

    /**
     * Create a {@code GKFileActions} that communicates with {@code card}.
     *
     * @param card the {@code GKCard} to be used with file actions
     * @since 0.5.0
     */
    public GKFileActions(GKCard card) {
        mCard = card;
    }

    /**
     * Retrieve a list of files stored at the given path on the GateKeeper Card.
     *
     * @param cardPath the path at which to retrieve the list of files
     * @return the {@code ListFilesResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public ListFilesResult listFiles(String cardPath) throws IOException {
        mCard.connect();
        Response response = mCard.list(cardPath);
        return new ListFilesResult(response, cardPath);
    }

    /**
     * Retrieve the given file from the GateKeeper Card.
     *
     * @param file      a {@code GKFile} referencing the file to retrieve
     * @param localFile a {@code File} to store retrieved file data
     * @return the {@code GetFileResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public GetFileResult getFile(final GKFile file, File localFile) throws IOException {
        mCard.connect();
        Response response = mCard.get(file.getCardPath());
        GetFileResult result = new GetFileResult(response, localFile);
        if (result.getStatus() == Status.SUCCESS) {
            FileOutputStream outputStream = new FileOutputStream(localFile);
            outputStream.write(response.getData());
        }
        return result;
    }

    /**
     * Store the given data to the given path on the GateKeeper Card.
     *
     * @param localFile a stream with file data
     * @param cardPath  the path at which to store the file data
     * @return the {@code FileResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public PutFileResult putFile(InputStream localFile, String cardPath) throws IOException {
        mCard.connect();
        Response response = mCard.put(cardPath, localFile);
        if (response.getStatus() != 226) {
            return new PutFileResult(response);
        }
        Response finalize = mCard.finalize(cardPath);
        return new PutFileResult(finalize);
    }

    /**
     * Delete the given file on the GateKeeper Card.
     *
     * @param file a {@code GKFile} referencing the file to delete
     * @return the {@code FileResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public FileResult deleteFile(GKFile file) throws IOException {
        mCard.connect();
        Response response;
        if (file.getType() == GKFile.Type.FILE) {
            response = mCard.delete(file.getCardPath());
        } else {
            response = mCard.deletePath(file.getCardPath());
        }
        return new FileResult(response);
    }

    /**
     * Create a directory at the given path on the GateKeeper Card.
     *
     * @param cardPath the path at which to create the directory
     * @return the {@code FileResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public FileResult makeDirectory(String cardPath) throws IOException {
        mCard.connect();
        Response response = mCard.createPath(cardPath);
        return new FileResult(response);
    }

    /**
     * Status is the named result of an action.
     */
    public enum Status {
        /**
         * The action was successful.
         */
        SUCCESS,

        /**
         * The client is not currently Authenticated with the GateKeeper Card.
         */
        UNAUTHORIZED,

        /**
         * The target path of the action could not be found.
         */
        NOT_FOUND,

        /**
         * The GateKeeper Card API returned a result that GKCardSettings does
         * not understand.
         */
        UNKNOWN_STATUS
    }

    /**
     * FileResult encapsulates the result of basic file actions.
     */
    public static class FileResult {
        /**
         * The {@code Response} received from the GateKeeper Card.
         */
        protected final Response mResponse;

        /**
         * The {@code Status} of the action.
         */
        protected final Status mStatus;

        /**
         * Create an {@code FileResult} to interpret the {@code Response}
         * received from the GateKeeper Card.
         *
         * @param response the {@code Response} received from the GateKeeper Card
         * @since 0.5.0
         */
        public FileResult(Response response) {
            mResponse = response;
            mStatus = parseResponseStatus(response);
        }

        /**
         * Create an {@code FileResult} with a {@code Status}
         * received from the GateKeeper Card.
         *
         * @param status the {@code Response} received from the GateKeeper Card
         * @since 0.5.0
         */
        public FileResult(Status status) {
            mResponse = null;
            mStatus = status;
        }

        /**
         * Retrieve the {@code Status} describing the {@code FileResult}.
         *
         * @return the {@code Status} of the {@code FileResult}
         * @since 0.5.0
         */
        public Status getStatus() {
            return mStatus;
        }

        private Status parseResponseStatus(Response response) {
            switch (response.getStatus()) {
                case 213:
                    return Status.SUCCESS;
                case 226:
                    return Status.SUCCESS;
                case 250:
                    return Status.SUCCESS;
                case 257:
                    return Status.SUCCESS;
                case 530:
                    return Status.UNAUTHORIZED;
                case 550:
                    return Status.NOT_FOUND;
                default:
                    return Status.UNKNOWN_STATUS;
            }
        }
    }

    /**
     * ListFilesResult encapsulates the result of the "List Files" action.
     */
    public static class ListFilesResult extends FileResult {
        /**
         * The list of files retrieved from the GateKeeper Card.
         */
        protected final List<GKFile> mFiles;

        /**
         * Create a {@code ListFilesResult} to interpret the {@code Response}
         * received from the GateKeeper Card.
         *
         * @param response the {@code Response} received from the GateKeeper Card
         * @param cardPath the path used in the action
         */
        public ListFilesResult(Response response, String cardPath) {
            super(response);
            mFiles = parseFileList(response.getData(), cardPath);
        }

        /**
         * Retrieve the files obtained from the GateKeeper Card.
         *
         * @return the list of files
         * @since 0.5.0
         */
        public List<GKFile> getFiles() {
            return mFiles;
        }

        private List<GKFile> parseFileList(byte[] response, String cardPath) {
            List<GKFile> filesList = new ArrayList<>();

            if (response == null) {
                return filesList;
            }

            String responseString = new String(response);
            Pattern pattern = Pattern.compile(".*\r\n");
            Matcher matcher = pattern.matcher(responseString);

            List<String> list = new ArrayList<>();

            while (matcher.find()) {
                list.add(matcher.group());
            }

            for (String fileString : list) {
                Matcher fileMatcher = GKFileUtils.FILE_PATTERN.matcher(fileString);
                if (fileMatcher.find()) {
                    String typeString = fileMatcher.group(1);
                    String name = fileMatcher.group(3);
                    GKFile.Type type = typeString.equals("d") ? GKFile.Type.DIRECTORY : GKFile.Type.FILE;
                    GKFile file = new GKFile(name, type);
                    file.setCardPath(cardPath, file.getName());
                    filesList.add(file);
                }
            }

            return filesList;
        }
    }

    /**
     * GetFileResult encapsulates the result of the "Get File" action.
     */
    public static class GetFileResult extends FileResult {
        /**
         * The file with data retrieved from the GateKeeper Card.
         */
        protected final File mFile;

        /**
         * Create a {@code GetFileResult} to interpret the {@code Response}
         * received from the GateKeeper Card.
         *
         * @param response the {@code Response} received from the GateKeeper Card
         * @param file     the {@code File} storing the retrieved file data
         */
        public GetFileResult(Response response, File file) {
            super(response);
            mFile = file;
        }

        /**
         * Retrieve the file storing the retrieved file data
         *
         * @return the file storing the retrieved file data
         */
        public File getFile() {
            return mFile;
        }
    }

    /**
     * PutFileResult encapsulates the result of the "Put File" action.
     */
    public static class PutFileResult extends FileResult {
        /**
         * The GKFile with file name returned from the put command
         */
        protected final GKFile mFile;

        /**
         * Create a {@code PutFileResult} to interpret the {@code Response}
         * received from the GateKeeper Card.
         *
         * @param response the {@code Response} received from the GateKeeper Card
         */
        public PutFileResult(Response response) {
            super(response);
            mFile = parseFileName();
        }

        /**
         * @return the {@code GKFile} that represents the created file
         */
        public GKFile getFile() {
            return mFile;
        }

        private GKFile parseFileName() {
            String data = mResponse.getMessage();
            if (data == null) {
                return null;
            }

            int pathStartIndex = data.indexOf(":") + 1;
            String filePath = data.substring(pathStartIndex);
            int lastFileSeparatorIndex = filePath.lastIndexOf("/");
            if (lastFileSeparatorIndex == -1) {
                return new GKFile(filePath, GKFile.Type.FILE);
            }

            String fileName = filePath.substring(lastFileSeparatorIndex + 1);
            GKFile gkFile = new GKFile(fileName, GKFile.Type.FILE);
            gkFile.setCardPath(filePath);
            return gkFile;
        }
    }
}
