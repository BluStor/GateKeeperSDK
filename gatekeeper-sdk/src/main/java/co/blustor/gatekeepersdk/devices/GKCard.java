package co.blustor.gatekeepersdk.devices;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * GKCard represents a client connection to a GateKeeper Card.
 */
public interface GKCard {
    /**
     * Send a `list` action to the GateKeeper Card.
     *
     * @param cardPath the path used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    Response list(String cardPath) throws IOException;

    /**
     * Send a `get` action to the GateKeeper Card.
     *
     * @param cardPath the path used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    Response get(String cardPath) throws IOException;

    /**
     * Send a `get` action to the GateKeeper Card.
     *
     * @param cardPath the path used in the action
     * @param localFile the local file used to store the response data
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.16.0
     */
    Response get(String cardPath, File localFile) throws IOException;

    /**
     * Send a `put` action to the GateKeeper Card.
     *
     * @param cardPath    the path used in the action
     * @param inputStream a stream with data used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    Response put(String cardPath, InputStream inputStream) throws IOException;

    /**
     * Send a `delete` action to the GateKeeper Card.
     *
     * @param cardPath the path used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    Response delete(String cardPath) throws IOException;

    /**
     * Send a `createPath` action to the GateKeeper Card.
     *
     * @param cardPath the path used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    Response createPath(String cardPath) throws IOException;

    /**
     * Send a `deletePath` action to the GateKeeper Card.
     *
     * @param cardPath the path used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    Response deletePath(String cardPath) throws IOException;

    /**
     * Send a `finalize` action to the GateKeeper Card.
     *
     * @param cardPath the path used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    Response finalize(String cardPath) throws IOException;

    /**
     * Open a connection with the GateKeeper Card.
     *
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    void connect() throws IOException;

    /**
     * Close the connection with the GateKeeper Card.
     *
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    void disconnect() throws IOException;

    /**
     * Get the current state of the connection between this object and the GateKeeper Card.
     *
     * @return the current {@code ConnectionState} between this object and the
     * GateKeeper Card.
     * @since 0.5.0
     */
    ConnectionState getConnectionState();

    /**
     * Intended for internal use only.
     *
     * @param state the {@code ConnectionState} that describes the new state of the connection
     */
    void onConnectionChanged(ConnectionState state);

    /**
     * Add a {@code Monitor} to be informed about changes to the state of the GateKeeper Card.
     *
     * @param monitor the {@code Monitor} to be added
     * @since 0.5.0
     */
    void addMonitor(Monitor monitor);

    /**
     * Remove a {@code Monitor} from the {@code GKCard}.
     *
     * @param monitor the {@code Monitor} to be removed
     * @since 0.5.0
     */
    void removeMonitor(Monitor monitor);

    /**
     * ConnectionState is the current state of the connection between a {@code GKCard} and
     * a GateKeeper Card.
     */
    enum ConnectionState {
        /**
         * Bluetooth is not enabled on the current device.
         */
        BLUETOOTH_DISABLED,

        /**
         * The current device has not been paired with the GateKeeper Card.
         */
        CARD_NOT_PAIRED,

        /**
         * The {@code GKCard} is attempting to connect with the GateKeeper Card.
         */
        CONNECTING,

        /**
         * The {@code GKCard} is connected with the GateKeeper Card.
         */
        CONNECTED,

        /**
         * The {@code GKCard} is transferring data to or from the GateKeeper Card.
         */
        TRANSFERRING,

        /**
         * The {@code GKCard} is disconnecting from the GateKeeper Card.
         */
        DISCONNECTING,

        /**
         * The {@code GKCard} is disconnected from the GateKeeper Card.
         */
        DISCONNECTED
    }

    /**
     * A Monitor is a handler for changes to {@code ConnectionState}.
     */
    interface Monitor {
        /**
         * Update the {@code Monitor} with a new {@code ConnectionState}.
         *
         * @param state the new {@code ConnectionState} of the card being monitored
         * @since 0.5.0
         */
        void onStateChanged(ConnectionState state);
    }

    /**
     * A Response captures the communication received from the GateKeeper Card during the
     * execution of an action.
     */
    class Response {
        /**
         * The numeric status code received at the conclusion of the action.
         */
        protected int mStatus;

        /**
         * The {@code String} message received at the conclusion of the action.
         */
        protected String mMessage;

        /**
         * The {@code File} representing the location that any data is stored from a response
         */
        protected File mDataFile;

        /**
         * Create a {@code Response} with the basic attributes of the given {@code Response}.
         *
         * @param response the {@code Response} object to copy
         * @since 0.16.0
         */
        public Response(Response response) {
            mStatus = response.getStatus();
            mMessage = response.getMessage();
            mDataFile = response.getDataFile();
        }

        /**
         * Create a {@code Response} with the given status code and message.
         *
         * @param status  the numeric status code to classify this response
         * @param message the {@code String} message to describe this response
         * @since 0.5.0
         */
        public Response(int status, String message) {
            mStatus = status;
            mMessage = message;
        }

        /**
         * Create a {@code Response} with the given status code, message, and dataFile.
         *
         * @param status   the numeric status code to classify this response
         * @param message  the {@code String} message to describe this response
         * @param dataFile the {@code File} that holds body data for this response
         * @since 0.16.0
         */
        public Response(int status, String message, File dataFile) {
            mStatus = status;
            mMessage = message;
            mDataFile = dataFile;
        }

        /**
         * Create a {@code Response} with the given command data.
         *
         * @param commandData the data containing the status code and message
         * @since 0.5.0
         */
        public Response(byte[] commandData) {
            this(commandData, null);
        }

        /**
         * Create a {@code Response} with the given command and body data.
         *
         * @param commandData  the data containing the status code and message
         * @param bodyDataFile the {@code File} containing body data
         * @since 0.16.0
         */
        public Response(byte[] commandData, File bodyDataFile) {
            String responseString = new String(commandData);
            String[] split = responseString.split("\\s", 2);
            mStatus = Integer.parseInt(split[0]);
            if (split.length > 1) {
                mMessage = split[1];
            }
            mDataFile = bodyDataFile;
        }

        /**
         * Retrieve the status code of the Response.
         *
         * @return the numeric status code
         * @since 0.5.0
         */
        public int getStatus() {
            return mStatus;
        }

        /**
         * Retrieve the message of the Response.
         *
         * @return the {@code String} message
         * @since 0.5.0
         */
        public String getMessage() {
            return mMessage;
        }

        /**
         * Retrieve the status message of the Response.
         *
         * @return the space-separated status code and message
         * @since 0.5.0
         */
        public String getStatusMessage() {
            return mStatus + " " + mMessage;
        }

        /**
         * Retrieve the body data of the Response.
         *
         * @return the {@code File} that hold the response data
         * @since 0.16.0
         */
        public File getDataFile() {
            return mDataFile;
        }

        /**
         * Assign the file that holds the Response data.
         *
         * @param dataFile the {@code File} that holds response data
         * @since 0.16.0
         */
        public void setDataFile(File dataFile) {
            mDataFile = dataFile;
        }
    }

    /**
     * An AbortResponse represents the deliberate termination of an action.
     */
    class AbortResponse extends Response {
        /**
         * Create an {@code AbortResponse}.
         *
         * @since 0.5.0
         */
        public AbortResponse() {
            super(426, "Aborted.");
        }
    }
}
