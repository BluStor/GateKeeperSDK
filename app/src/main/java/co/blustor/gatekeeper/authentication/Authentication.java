package co.blustor.gatekeeper.authentication;

import com.neurotec.biometrics.NSubject;

import java.io.IOException;
import java.util.List;

import co.blustor.gatekeeper.devices.GKCard.Response;

public interface Authentication {
    Status signInWithFace(NSubject testSubject) throws IOException;
    Status enrollWithFace(NSubject subject) throws IOException;
    Status signOut() throws IOException;
    Status revokeFace() throws IOException;
    List<Object> listTemplates() throws IOException;

    enum Status {
        SUCCESS,
        AUTHENTICATED,
        CANCELED,
        UNAUTHENTICATED,
        UNAUTHORIZED,
        BAD_TEMPLATE,
        NOT_FOUND,
        UNKNOWN_STATUS;

        public static Status fromCardResponse(Response response) {
            switch (response.getStatus()) {
                case 226:
                    return Status.SUCCESS;
                case 230:
                    return Status.AUTHENTICATED;
                case 231:
                    return Status.SUCCESS;
                case 250:
                    return Status.SUCCESS;
                case 426:
                    return Status.CANCELED;
                case 430:
                    return Status.UNAUTHENTICATED;
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
}
