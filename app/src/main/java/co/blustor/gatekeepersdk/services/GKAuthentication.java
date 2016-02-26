package co.blustor.gatekeepersdk.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.blustor.gatekeepersdk.biometrics.GKFaces;
import co.blustor.gatekeepersdk.devices.GKCard;
import co.blustor.gatekeepersdk.devices.GKCard.Response;

/**
 * GKAuthentication is a Service for using facial authentication with the GateKeeper Card.
 */
public class GKAuthentication {
    public static final String TAG = GKAuthentication.class.getSimpleName();

    public static final String AUTH_ENROLL_PIN = "auth/enroll/pin";
    public static final String SIGN_IN_FACE_PATH = "/auth/signin/face";
    public static final String SIGN_OUT_PATH = "/auth/signout";
    public static final String SIGN_IN_PIN_PATH = "/auth/signin/pin";
    public static final String ENROLL_FACE_PATH_PREFIX = "/auth/enroll/face/";
    public static final String REVOKE_FACE_PATH_PREFIX = "/auth/enroll/face/";
    public static final String LIST_FACE_PATH = "/auth/enroll/face";
    public static final String LIST_PIN_PATH = "/auth/enroll/pin";
    protected final GKCard mCard;

    /**
     * Create a {@code GKAuthentication} that communicates with {@code card}.
     *
     * @param card the {@code GKCard} to be used with facial authentication actions
     * @since 0.5.0
     */
    public GKAuthentication(GKCard card) {
        mCard = card;
    }

    /**
     * Store a {@code Template} at the first template index on the GateKeeper Card.
     *
     * @param template the {@code Template} to be stored
     * @return the {@code AuthResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public AuthResult enrollWithFace(GKFaces.Template template) throws IOException {
        return enrollWithFace(template, 0);
    }

    public AuthResult enrollWithPin(String pin) throws IOException {
        ByteArrayInputStream inputStream = getInputStreamWithBytes(pin);
        return submitInputStream(inputStream, AUTH_ENROLL_PIN);
    }

    /**
     * Store a {@code Template} at the given template index on the GateKeeper Card.
     *
     * @param template   the {@code Template} to be stored
     * @param templateId the index at which to store the {@code template}
     * @return the {@code AuthResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public AuthResult enrollWithFace(GKFaces.Template template, int templateId) throws IOException {
        if (template.getQuality() != GKFaces.Template.Quality.OK) {
            return new AuthResult(GKAuthentication.Status.BAD_TEMPLATE);
        }
        return submitTemplate(template, ENROLL_FACE_PATH_PREFIX + templateId);
    }

    /**
     * Authenticate with the GateKeeper Card using a {@code Template}.
     *
     * @param template the {@code Template} to be submitted for authentication
     * @return the {@code AuthResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public AuthResult signInWithFace(GKFaces.Template template) throws IOException {
        if (template.getQuality() != GKFaces.Template.Quality.OK) {
            return new AuthResult(GKAuthentication.Status.BAD_TEMPLATE);
        }
        return submitTemplate(template, SIGN_IN_FACE_PATH);
    }

    /**
     * Authenticate with the GateKeeper Card using a {@code PIN}.
     *
     * @param pin the {@code PIN} to be submitted for authentication
     * @return the {@code AuthResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public AuthResult signInWithPin(String pin) throws IOException {
        ByteArrayInputStream inputStream = getInputStreamWithBytes(pin);
        return submitInputStream(inputStream, SIGN_IN_PIN_PATH);
    }

    /**
     * End the current session on the GateKeeper Card.
     *
     * @return the {@code AuthResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public AuthResult signOut() throws IOException {
        mCard.connect();
        Response response = mCard.delete(SIGN_OUT_PATH);
        return new AuthResult(response);
    }

    /**
     * Delete the {@code Template} at the first template index on the GateKeeper Card.
     *
     * @return the {@code AuthResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public AuthResult revokeFace() throws IOException {
        return revokeFace(0);
    }

    /**
     * Delete the PIN on the GateKeeper Card.
     *
     * @return the {@code AuthResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.6.1
     */
    public AuthResult revokePin() throws IOException {
        mCard.connect();
        return new AuthResult(mCard.delete(AUTH_ENROLL_PIN));
    }

    /**
     * Delete the {@code Template} at the given template index on the GateKeeper Card.
     *
     * @param templateId the index at which to delete a {@code template}
     * @return the {@code AuthResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public AuthResult revokeFace(int templateId) throws IOException {
        mCard.connect();
        Response response = mCard.delete(REVOKE_FACE_PATH_PREFIX + templateId);
        return new AuthResult(response);
    }

    /**
     * Retrieve the list of face templates stored on the GateKeeper Card.
     *
     * @return the {@code ListTemplatesResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.8.1
     */
    public ListTemplatesResult listFaceTemplates() throws IOException {
        mCard.connect();
        Response response = mCard.list(LIST_FACE_PATH);
        return new ListTemplatesResult(response);
    }

    /**
     * Retrieve the list of PIN templates stored on the GateKeeper Card.
     *
     * @return the {@code ListTemplatesResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.8.1
     */
    public ListTemplatesResult listPinTemplates() throws IOException {
        mCard.connect();
        Response response = mCard.list(LIST_PIN_PATH);
        return new ListTemplatesResult(response);
    }

    private ByteArrayInputStream getInputStreamWithBytes(String s) {
        byte[] bytes = s.getBytes(Charset.defaultCharset());
        return new ByteArrayInputStream(bytes);
    }

    private AuthResult submitTemplate(GKFaces.Template template, String cardPath) throws IOException {
        InputStream inputStream = template.getInputStream();
        return submitInputStream(inputStream, cardPath);
    }

    private AuthResult submitInputStream(InputStream inputStream, String cardPath) throws IOException {
        try {
            mCard.connect();
            Response response = mCard.put(cardPath, inputStream);
            if (response.getStatus() != 226) {
                return new AuthResult(response);
            }
            return new AuthResult(mCard.finalize(cardPath));
        } finally {
            inputStream.close();
        }
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
         * The given template data was successfully stored on the GateKeeper Card.
         */
        TEMPLATE_ADDED,

        /**
         * The client has successfully started a session on the GateKeeper Card.
         */
        SIGNED_IN,

        /**
         * The client has ended the current session on the GateKeeper Card.
         */
        SIGNED_OUT,

        /**
         * The client did not successfully start a session on the GateKeeper Card.
         */
        SIGN_IN_FAILURE,

        /**
         * The client is not currently Authenticated with the GateKeeper Card.
         */
        UNAUTHORIZED,

        /**
         * The given template data was not acceptable.
         */
        BAD_TEMPLATE,

        /**
         * The target path of the action could not be found.
         */
        NOT_FOUND,

        /**
         * The action was terminated by request.
         */
        CANCELED,

        /**
         * The GateKeeper Card API returned a result that GKAuthentication does
         * not understand.
         */
        UNKNOWN_STATUS
    }

    /**
     * AuthResult encapsulates the result of basic authentication actions.
     */
    public static class AuthResult {
        /**
         * The {@code Response} received from the GateKeeper Card.
         */
        protected final Response mResponse;

        /**
         * The {@code Status} of the action.
         */
        protected final Status mStatus;

        /**
         * Create an {@code AuthResult} to interpret the {@code Response}
         * received from the GateKeeper Card.
         *
         * @param response the {@code Response} received from the GateKeeper Card
         * @since 0.5.0
         */
        public AuthResult(Response response) {
            mResponse = response;
            mStatus = parseResponseStatus(mResponse);
        }

        /**
         * Create an {@code AuthResult} without a full {@code Response}
         * from the GateKeeper Card.
         *
         * @param status a {@code Status} to describe the {@code AuthResult}
         * @since 0.5.0
         */
        public AuthResult(Status status) {
            mResponse = null;
            mStatus = status;
        }

        /**
         * Retrieve the {@code Status} describing the {@code AuthResult}.
         *
         * @return the {@code Status} of the {@code AuthResult}
         * @since 0.5.0
         */
        public Status getStatus() {
            return mStatus;
        }

        private static Status parseResponseStatus(Response response) {
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

    /**
     * ListTemplatesResult encapsulates the result of the "List Templates" action.
     */
    public class ListTemplatesResult extends AuthResult {
        /**
         * An unknown template is present when the action was performed without having
         * a valid session established on the GateKeeper Card.
         */
        public static final String UNKNOWN_TEMPLATE = "UNKNOWN_TEMPLATE";

        /**
         * The list of templates retrieved from the GateKeeper Card.
         */
        protected final List<Object> mTemplates;

        private Pattern mFilePattern = Pattern.compile("([-d])\\S+(\\S+\\s+){8}(.*)$");

        /**
         * Create a {@code ListTemplatesResult} to interpret the {@code Response}
         * received from the GateKeeper Card.
         *
         * @param response the {@code Response} received from the GateKeeper Card
         * @since 0.5.0
         */
        public ListTemplatesResult(Response response) {
            super(response);
            mTemplates = parseTemplates();
        }

        /**
         * Create a {@code ListTemplatesResult} to interpret the {@code Response}
         * received from the GateKeeper Card.
         *
         * @param response  the {@code Response} received from the GateKeeper Card
         * @param templates the List of templates received from the GateKeeper Card
         * @since 0.8.1
         */
        public ListTemplatesResult(Response response, List<Object> templates) {
            super(response);
            mTemplates = templates;
        }

        /**
         * Retrieve the templates obtained from the GateKeeper Card.
         *
         * @return the list of templates
         * @since 0.5.0
         */
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
                    list.add(template);
                }
            }
            return list;
        }

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
    }
}
