package co.blustor.gatekeepersdk.services;

import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.blustor.gatekeepersdk.data.GKCardConfiguration;
import co.blustor.gatekeepersdk.devices.GKCard;
import co.blustor.gatekeepersdk.devices.GKCard.Response;

/**
 * GKCardSettings is a Service for handling GateKeeper Card configuration data.
 */
public class GKCardSettings {
    private static final String UPDATE_FIRMWARE_PATH = "/device/firmware";
    private static final String GET_FIRMWARE_INFO_PATH = "/device/firmware";
    private static final String GET_CARD_SETTINGS_PATH = "/device/settings";

    private final GKCard mCard;

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
        Response response = mCard.put(UPDATE_FIRMWARE_PATH, inputStream);
        if (response.getStatus() != 226) {
            return new CardResult(response);
        }
        Response finalize = mCard.finalize(UPDATE_FIRMWARE_PATH);
        return new CardResult(finalize);
    }

    /**
     * Retrieves Firmware data from the GateKeeper Card.
     *
     * @return the {@code FirmwareInformationResult} of the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.6.0
     */
    public FirmwareInformationResult getFirmwareInformation() throws IOException {
        Response response = mCard.get(GET_FIRMWARE_INFO_PATH);
        return new FirmwareInformationResult(response);
    }

    /**
     * Retrieves card settings file
     *
     * @return the {@code CardSettingsResult} which holds the card settings
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.17.0
     */
    public CardSettingsResult getCardSettings() throws IOException {
        Response response = mCard.get(GET_CARD_SETTINGS_PATH);
        return new CardSettingsResult(response);
    }

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
         * The file could not be found on the card
         */
        NOT_FOUND,

        /**
         * The GateKeeper Card API returned a result that GKCardSettings does
         * not understand.
         */
        UNKNOWN_STATUS
    }

    /**
     * CardResult encapsulates the result of basic settings actions.
     */
    public static class CardResult {
        /**
         * The {@code Response} received from the GateKeeper Card.
         */
        protected Response mResponse;

        /**
         * The {@code Status} of the action.
         */
        protected Status mStatus;

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

        private Status parseResponseStatus(Response response) {
            switch (response.getStatus()) {
                case 213:
                    return Status.SUCCESS;
                case 226:
                    return Status.SUCCESS;
                case 501:
                    return Status.INVALID_DATA;
                case 530:
                    return Status.UNAUTHORIZED;
                case 550:
                    return Status.NOT_FOUND;
                default:
                    return Status.UNKNOWN_STATUS;
            }
        }
    }

    public static class FirmwareInformationResult extends CardResult {
        private static final String TAG = FirmwareInformationResult.class.getCanonicalName();
        private static final String BOOT_VERSION_PATTERN = "BOOT:\\s*(\\S*)";
        private static final String FIRMWARE_VERSION_PATTERN = "FIRM:\\s*(\\S*)";

        private String mBootVersion;
        private String mFirmwareVersion;

        /**
         * Create an {@code FirmwareInformationResult} to interpret the {@code Response}
         * received from the GateKeeper Card.
         *
         * @param response the {@code Response} received from the GateKeeper Card
         */
        public FirmwareInformationResult(Response response) {
            super(response);
            mBootVersion = parseVersion(BOOT_VERSION_PATTERN);
            mFirmwareVersion = parseVersion(FIRMWARE_VERSION_PATTERN);

            if (mStatus == Status.SUCCESS && (mBootVersion == null || mFirmwareVersion == null)) {
                mStatus = Status.UNKNOWN_STATUS;
            }
        }

        /**
         * Retrieve the bootloader version returned in the {@code Response} from
         * calling the GateKeeper Card's firmware information endpoint.
         *
         * @return the {@code String} of the bootloader version
         */
        public String getBootVersion() {
            return mBootVersion;
        }

        /**
         * Retrieve the firmware version returned in the {@code Response} from
         * calling the GateKeeper Card's firmware information endpoint.
         *
         * @return the {@code String} of the firmware version
         */
        public String getFirmwareVersion() {
            return mFirmwareVersion;
        }

        private String parseVersion(String versionPattern) {
            String data = mResponse.readDataFile();
            Pattern pattern = Pattern.compile(versionPattern);
            Matcher matcher = pattern.matcher(data);
            return matcher.find() ? matcher.group(1) : null;
        }
    }

    public static class CardSettingsResult extends CardResult {
        private GKCardConfiguration mCardConfig;

        /**
         * Create an {@code CardSettingsResult} to interpret the {@code Response}
         * received from the GateKeeper Card.
         *
         * @param response the {@code Response} received from the GateKeeper Card
         * @since 0.17.0
         */
        public CardSettingsResult(Response response) {
            super(response);
            mCardConfig = new GKCardConfiguration(response.readDataFile());
        }

        public GKCardConfiguration getCardConfig() {
            return mCardConfig;
        }
    }
}