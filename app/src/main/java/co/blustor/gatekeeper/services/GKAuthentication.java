package co.blustor.gatekeeper.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.blustor.gatekeeper.biometrics.GKFaces;
import co.blustor.gatekeeper.devices.GKCard;
import co.blustor.gatekeeper.devices.GKCard.Response;

public class GKAuthentication {
    public static final String TAG = GKAuthentication.class.getSimpleName();

    public static final String SIGN_IN_PATH = "/auth/signin";
    public static final String SIGN_OUT_PATH = "/auth/signout";
    public static final String ENROLL_FACE_PATH_PREFIX = "/auth/face00";
    public static final String REVOKE_FACE_PATH_PREFIX = "/auth/face00";
    public static final String LIST_FACE_PATH = "/auth";

    public enum Status {
        SUCCESS,
        TEMPLATE_ADDED,
        SIGNED_IN,
        SIGNED_OUT,
        SIGN_IN_FAILURE,
        UNAUTHORIZED,
        BAD_TEMPLATE,
        NOT_FOUND,
        CANCELED,
        UNKNOWN_STATUS
    }

    protected final GKCard mCard;

    public GKAuthentication(GKCard card) {
        mCard = card;
    }

    public AuthResult enrollWithFace(GKFaces.Template template) throws IOException {
        return enrollWithFace(template, 0);
    }

    public AuthResult enrollWithFace(GKFaces.Template template, int templateId) throws IOException {
        if (template.getQuality() != GKFaces.Template.Quality.OK) {
            return new AuthResult(GKAuthentication.Status.BAD_TEMPLATE);
        }
        Response response = submitTemplate(template, ENROLL_FACE_PATH_PREFIX + templateId);
        return new AuthResult(response);
    }

    public AuthResult signInWithFace(GKFaces.Template template) throws IOException {
        if (template.getQuality() != GKFaces.Template.Quality.OK) {
            return new AuthResult(GKAuthentication.Status.BAD_TEMPLATE);
        }
        Response response = submitTemplate(template, SIGN_IN_PATH);
        return new AuthResult(response);
    }

    public AuthResult signOut() throws IOException {
        Response response = mCard.delete(SIGN_OUT_PATH);
        return new AuthResult(response);
    }

    public AuthResult revokeFace() throws IOException {
        return revokeFace(0);
    }

    public AuthResult revokeFace(int templateId) throws IOException {
        Response response = mCard.delete(REVOKE_FACE_PATH_PREFIX + templateId);
        return new AuthResult(response);
    }

    public ListTemplatesResult listTemplates() throws IOException {
        Response response = mCard.list(LIST_FACE_PATH);
        return new ListTemplatesResult(response);
    }

    private final Pattern mFilePattern = Pattern.compile("([-d])\\S+(\\S+\\s+){8}(.*)$");

    private List<String> parseTemplateList(byte[] response) {
        String responseString = new String(response);

        Pattern pattern = Pattern.compile(".*\r\n");
        Matcher matcher = pattern.matcher(responseString);

        List<String> lineList = new ArrayList<>();

        while (matcher.find()) {
            lineList.add(matcher.group());
        }

        List<String> templateList = new ArrayList<>();

        for (String fileString : lineList) {
            Matcher fileMatcher = mFilePattern.matcher(fileString);
            if (fileMatcher.find()) {
                String typeString = fileMatcher.group(1);
                String name = fileMatcher.group(3);
                if (typeString.equals("d")) {
                    continue;
                }
                templateList.add(name);
            }
        }

        return templateList;
    }

    private Response submitTemplate(GKFaces.Template template, String cardPath) throws IOException {
        mCard.connect();
        InputStream inputStream = template.getInputStream();
        try {
            Response response = mCard.put(cardPath, inputStream);
            if (response.getStatus() != 226) {
                return response;
            }
            return mCard.finalize(cardPath);
        } finally {
            inputStream.close();
        }
    }

    public class ListTemplatesResult extends AuthResult {
        public static final String UNKNOWN_TEMPLATE = "UNKNOWN_TEMPLATE";

        protected final List<Object> mTemplates;

        public ListTemplatesResult(Response response) {
            super(response);
            mTemplates = parseTemplates();
        }

        public List<Object> getTemplates() {
            return mTemplates;
        }

        private List<Object> parseTemplates() {
            List<Object> list = new ArrayList<>();
            if (mStatus == Status.UNAUTHORIZED) {
                list.add(UNKNOWN_TEMPLATE);
            } else {
                if (mResponse.getData() == null) {
                    return list;
                }
                List<String> templates = parseTemplateList(mResponse.getData());
                for (String template : templates) {
                    if (template.startsWith("face")) {
                        list.add(template);
                    }
                }
            }
            return list;
        }
    }

    public class AuthResult {
        protected final Response mResponse;
        protected final Status mStatus;

        public AuthResult(Response response) {
            mResponse = response;
            mStatus = parseResponseStatus(mResponse);
        }

        public AuthResult(Status status) {
            mResponse = null;
            mStatus = status;
        }

        public Status getStatus() {
            return mStatus;
        }
    }

    private Status parseResponseStatus(Response response) {
        switch (response.getStatus()) {
            case 213:
                return Status.TEMPLATE_ADDED;
            case 226:
                return Status.SUCCESS;
            case 230:
                return Status.SIGNED_IN;
            case 231:
                return Status.SIGNED_OUT;
            case 250:
                return Status.SUCCESS;
            case 426:
                return Status.CANCELED;
            case 430:
                return Status.SIGN_IN_FAILURE;
            case 501:
                return Status.BAD_TEMPLATE;
            case 530:
                return Status.UNAUTHORIZED;
            case 550:
                return Status.NOT_FOUND;
            default:
                return Status.UNKNOWN_STATUS;
        }
    }
}
