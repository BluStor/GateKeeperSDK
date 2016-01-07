package co.blustor.gatekeeper.scopes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.blustor.gatekeeper.data.GKFile;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCard.Response;

public class GKFileActions {
    public static final String TAG = GKFileActions.class.getSimpleName();

    public enum Status {
        SUCCESS,
        UNAUTHORIZED,
        NOT_FOUND,
        UNKNOWN_STATUS
    }

    private final GKCard mCard;

    public GKFileActions(GKCard card) {
        mCard = card;
    }

    public ListFilesResult listFiles(String remotePath) throws IOException {
        Response response = mCard.list(remotePath);
        return new ListFilesResult(response, remotePath);
    }

    public File getFile(final GKFile gkFile, File localFile) throws IOException {
        Response response = mCard.get(gkFile.getCardPath());
        FileOutputStream outputStream = new FileOutputStream(localFile);
        outputStream.write(response.getData());
        return localFile;
    }

    public boolean putFile(InputStream localFile, String remotePath) throws IOException {
        Response response = mCard.put(remotePath, localFile);
        if (response.getStatus() != 226) {
            return false;
        }
        Response finalize = mCard.finalize(remotePath);
        return finalize.getStatus() == 213;
    }

    public boolean deleteFile(GKFile file) throws IOException {
        if (file.getType() == GKFile.Type.FILE) {
            Response response = mCard.delete(file.getCardPath());
            return response.getStatus() == 250;
        } else {
            Response response = mCard.deletePath(file.getCardPath());
            return response.getStatus() == 250;
        }
    }

    public boolean makeDirectory(String fullPath) throws IOException {
        Response response = mCard.createPath(fullPath);
        return response.getStatus() == 257;
    }

    private final Pattern mFilePattern = Pattern.compile("([-d])\\S+(\\S+\\s+){8}(.*)$");

    public class ListFilesResult {
        protected final Response mResponse;
        protected final Status mStatus;
        protected final List<GKFile> mFiles;

        public ListFilesResult(Response response, String remotePath) {
            mResponse = response;
            mStatus = parseResponseStatus(mResponse);
            mFiles = parseFileList(response.getData(), remotePath);
        }

        public Status getStatus() {
            return mStatus;
        }

        public List<GKFile> getFiles() {
            return mFiles;
        }

        private List<GKFile> parseFileList(byte[] response, String remotePath) {
            String responseString = new String(response);

            Pattern pattern = Pattern.compile(".*\r\n");
            Matcher matcher = pattern.matcher(responseString);

            List<String> list = new ArrayList<>();

            while (matcher.find()) {
                list.add(matcher.group());
            }

            List<GKFile> filesList = new ArrayList<>();

            for (String fileString : list) {
                Matcher fileMatcher = mFilePattern.matcher(fileString);
                if (fileMatcher.find()) {
                    String typeString = fileMatcher.group(1);
                    String name = fileMatcher.group(3);
                    GKFile.Type type = typeString.equals("d") ? GKFile.Type.DIRECTORY : GKFile.Type.FILE;
                    GKFile file = new GKFile(name, type);
                    file.setCardPath(remotePath, file.getName());
                    filesList.add(file);
                }
            }

            return filesList;
        }
    }

    private Status parseResponseStatus(Response response) {
        switch (response.getStatus()) {
            case 226:
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
