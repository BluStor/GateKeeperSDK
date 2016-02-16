package co.blustor.gatekeepersdk.services;

import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeepersdk.devices.GKCard;
import co.blustor.gatekeepersdk.devices.GKCard.Response;

/**
 * GKCardSettings is a Service for handling GateKeeper Card configuration data.
 */
public class GKCardSettings {
    private static final String UPDATE_FIRMWARE_PATH = "/device/firmware";

    private final GKCard mCard;

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
         * The given data was not acceptable.
         */
        INVALID_DATA,

        /**
         * The GateKeeper Card API returned a result that GKCardSettings does
         * not understand.
         */
        UNKNOWN_STATUS
    }

    /**
     * Create a {@code GKCardSettings} that communicates with {@code card}.
     *
     * @param card the {@code GKCard} to be used with settings actions
     * @since 0.5.0
     */
    public GKCardSettings(GKCard card) {
        mCard = card;
    }

    /**
     * Send Firmware data to the GateKeeper Card.
     *
     * @param inputStream a stream with Firmware data
     * @return the {@code CardResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public CardResult updateFirmware(InputStream inputStream) throws IOException {
        mCard.connect();
        Response response = mCard.put(UPDATE_FIRMWARE_PATH, inputStream);
        if (response.getStatus() != 226) {
            return new CardResult(response);
        }
        Response finalize = mCard.finalize(UPDATE_FIRMWARE_PATH);
        return new CardResult(finalize);
    }

    /**
     * CardResult encapsulates the result of basic settings actions.
     */
    public class CardResult {
        /**
         * The {@code Response} received from the GateKeeper Card.
         */
        protected final Response mResponse;

        /**
         * The {@code Status} of the action.
         */
        protected final Status mStatus;

        /**
         * Create an {@code CardResult} to interpret the {@code Response}
         * received from the GateKeeper Card.
         *
         * @param response the {@code Response} received from the GateKeeper Card
         * @since 0.5.0
         */
        public CardResult(Response response) {
            mResponse = response;
            mStatus = parseResponseStatus(response);
        }

        /**
         * Retrieve the {@code Status} describing the {@code CardResult}.
         *
         * @return the {@code Status} of the {@code CardResult}
         * @since 0.5.0
         */
        public Status getStatus() {
            return mStatus;
        }
    }

    private Status parseResponseStatus(Response response) {
        switch (response.getStatus()) {
            case 213:
                return Status.SUCCESS;
            case 501:
                return Status.INVALID_DATA;
            case 530:
                return Status.UNAUTHORIZED;
            default:
                return Status.UNKNOWN_STATUS;
        }
    }
}
