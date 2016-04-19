package co.blustor.gatekeepersdk.devices;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.blustor.gatekeepersdk.data.GKMultiplexer;
import co.blustor.gatekeepersdk.utils.GKStringUtils;

public abstract class GKCard {
    private static final String LIST = "LIST";
    private static final String RETR = "RETR";
    private static final String STOR = "STOR";
    private static final String DELE = "DELE";
    private static final String MKD = "MKD";
    private static final String RMD = "RMD";
    private static final String SRFT = "SRFT";
    protected GKMultiplexer mMultiplexer;
    private List<Monitor> mCardMonitors = new ArrayList<>();
    private ConnectionState mConnectionState = ConnectionState.DISCONNECTED;

    /**
     * Send a `list` action to the GateKeeper Card.
     *
     * @param cardPath the path used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public Response list(String cardPath) throws IOException {
        cardPath = globularPath(cardPath);
        return get(LIST, cardPath);
    }

    /**
     * Send a `get` action to the GateKeeper Card.
     *
     * @param cardPath the path used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public Response get(String cardPath) throws IOException {
        return get(RETR, cardPath);
    }

    /**
     * Send a `put` action to the GateKeeper Card.
     *
     * @param cardPath    the path used in the action
     * @param inputStream a stream with data used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public Response put(String cardPath, InputStream inputStream) throws IOException {
        try {
            onConnectionChanged(ConnectionState.TRANSFERRING);
            sendCommand(STOR, cardPath);
            Response commandResponse = getCommandResponse();
            if (commandResponse.getStatus() != 150) {
                onConnectionChanged(ConnectionState.CONNECTED);
                return commandResponse;
            }
            mMultiplexer.writeToDataChannel(inputStream);
            Response dataResponse = getCommandResponse();
            onConnectionChanged(ConnectionState.CONNECTED);
            return dataResponse;
        } catch (InterruptedException e) {
            logCommandInterruption(STOR, cardPath, e);
            onConnectionChanged(ConnectionState.CONNECTED);
            return new AbortResponse();
        }
    }

    /**
     * Send a `delete` action to the GateKeeper Card.
     *
     * @param cardPath the path used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public Response delete(String cardPath) throws IOException {
        return call(DELE, cardPath);
    }

    /**
     * Send a `createPath` action to the GateKeeper Card.
     *
     * @param cardPath the path used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public Response createPath(String cardPath) throws IOException {
        return call(MKD, cardPath);
    }

    /**
     * Send a `deletePath` action to the GateKeeper Card.
     *
     * @param cardPath the path used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public Response deletePath(String cardPath) throws IOException {
        return call(RMD, cardPath);
    }

    /**
     * Send a `finalize` action to the GateKeeper Card.
     *
     * @param cardPath the path used in the action
     * @return a {@code Response} with information about the action
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public Response finalize(String cardPath) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        return call(SRFT, timestamp + " " + cardPath);
    }

    protected abstract GKMultiplexer connectToMultiplexer() throws IOException;

    protected abstract boolean isDisconnected();

    /**
     * Open a connection with the GateKeeper Card.
     *
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public void connect() throws IOException {
        if (isDisconnected()) {
//            disconnect();
            onConnectionChanged(ConnectionState.CONNECTING);
            try {
                mMultiplexer = connectToMultiplexer();
                mMultiplexer.connect();
                onConnectionChanged(ConnectionState.CONNECTED);
            } catch (IOException e) {
                mMultiplexer = null;
                onConnectionChanged(ConnectionState.DISCONNECTED);
                throw e;
            }
        }
    }

    /**
     * Close the connection with the GateKeeper Card.
     *
     * @throws IOException when communication with the GateKeeper Card has been disrupted.
     * @since 0.5.0
     */
    public void disconnect() throws IOException {
        onConnectionChanged(ConnectionState.DISCONNECTING);
        if (mMultiplexer != null) {
            try {
                mMultiplexer.disconnect();
            } finally {
                mMultiplexer = null;
            }
        }
        onConnectionChanged(ConnectionState.DISCONNECTED);
    }

    /**
     * Get the current state of the connection between this object and the GateKeeper Card.
     *
     * @return the current {@code ConnectionState} between this object and the
     * GateKeeper Card.
     * @since 0.5.0
     */
    public ConnectionState getConnectionState() {
        synchronized (mCardMonitors) {
            return mConnectionState;
        }
    }

    /**
     * Intended for internal use only.
     * @param state the {@code ConnectionState} that describes the new state of the connection
     */
    public void onConnectionChanged(ConnectionState state) {
        synchronized (mCardMonitors) {
            if (mConnectionState.equals(state)) {
                return;
            }
            mConnectionState = state;
            if (state.equals(ConnectionState.DISCONNECTING) || state.equals(ConnectionState.DISCONNECTED)) {
                mMultiplexer = null;
            }
            for (Monitor monitor : mCardMonitors) {
                monitor.onStateChanged(state);
            }
        }
    }

    /**
     * Add a {@code Monitor} to be informed about changes to the state of the GateKeeper Card.
     *
     * @param monitor the {@code Monitor} to be added
     * @since 0.5.0
     */
    public void addMonitor(Monitor monitor) {
        synchronized (mCardMonitors) {
            if (!mCardMonitors.contains(monitor)) {
                mCardMonitors.add(monitor);
            }
        }
    }

    /**
     * Remove a {@code Monitor} from the {@code GKCard}.
     *
     * @param monitor the {@code Monitor} to be removed
     * @since 0.5.0
     */
    public void removeMonitor(Monitor monitor) {
        synchronized (mCardMonitors) {
            if (mCardMonitors.contains(monitor)) {
                mCardMonitors.remove(monitor);
            }
        }
    }

    private Response get(String method, String cardPath) throws IOException {
        try {
            onConnectionChanged(ConnectionState.TRANSFERRING);
            sendCommand(method, cardPath);
            Response commandResponse = getCommandResponse();
            if (commandResponse.getStatus() != 150) {
                onConnectionChanged(ConnectionState.CONNECTED);
                return commandResponse;
            }

            Response dataResponse = getCommandResponse();
            byte[] data = mMultiplexer.readDataChannel();
            dataResponse.setData(data);
            onConnectionChanged(ConnectionState.CONNECTED);
            return dataResponse;
        } catch (InterruptedException e) {
            logCommandInterruption(method, cardPath, e);
            onConnectionChanged(ConnectionState.CONNECTED);
            return new AbortResponse();
        }
    }

    private Response call(String method, String cardPath) throws IOException {
        try {
            onConnectionChanged(ConnectionState.TRANSFERRING);
            sendCommand(method, cardPath);
            Response commandResponse = getCommandResponse();
            onConnectionChanged(ConnectionState.CONNECTED);
            return commandResponse;
        } catch (InterruptedException e) {
            logCommandInterruption(method, cardPath, e);
            onConnectionChanged(ConnectionState.CONNECTED);
            return new AbortResponse();
        }
    }

    private void sendCommand(String method, String argument) throws IOException {
        checkMultiplexer();
        String cmd = buildCommandString(method, argument);
        Log.i(GKBluetoothCard.TAG, "Sending Command: '" + cmd.trim() + "'");
        byte[] bytes = getCommandBytes(cmd);
        mMultiplexer.writeToCommandChannel(bytes);
    }

    private Response getCommandResponse() throws IOException, InterruptedException {
        checkMultiplexer();
        Response response = new Response(mMultiplexer.readCommandChannelLine());
        Log.i(GKBluetoothCard.TAG, "Card Response: '" + response.getStatusMessage() + "'");
        return response;
    }

    private byte[] getCommandBytes(String cmd) {
        return (cmd + "\r\n").getBytes(StandardCharsets.US_ASCII);
    }

    private String buildCommandString(String method, String... arguments) {
        return String.format("%s %s", method, GKStringUtils.join(arguments, " "));
    }

    private String globularPath(String cardPath) {
        if (cardPath.equals("/")) {
            cardPath += "*";
        } else {
            cardPath += "/*";
        }
        return cardPath;
    }

    private void checkMultiplexer() throws IOException {
        if (mMultiplexer == null) {
            throw new IOException("Not Connected");
        }
    }

    private void logCommandInterruption(String method, String cardPath, InterruptedException e) {
        String commandString = buildCommandString(method, cardPath);
        Log.e(GKBluetoothCard.TAG, "'" + commandString + "' interrupted", e);
    }

    /**
     * ConnectionState is the current state of the connection between a {@code GKCard} and
     * a GateKeeper Card.
     */
    public enum ConnectionState {
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
    public interface Monitor {
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
    public static class Response {
        /**
         * The numeric status code received at the conclusion of the action.
         */
        protected int mStatus;

        /**
         * The {@code String} message received at the conclusion of the action.
         */
        protected String mMessage;

        /**
         * Any data received during execution of the action.
         */
        protected byte[] mData;

        /**
         * Create a {@code Response} with the basic attributes of the given {@code Response}.
         *
         * @param response the {@code Response} object to copy
         * @since 0.5.0
         */
        public Response(Response response) {
            mStatus = response.getStatus();
            mMessage = response.getMessage();
            mData = response.getData();
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
         * @param commandData the data containing the status code and message
         * @param bodyData    the data of the body
         * @since 0.5.0
         */
        public Response(byte[] commandData, byte[] bodyData) {
            String responseString = new String(commandData);
            String[] split = responseString.split("\\s", 2);
            mStatus = Integer.parseInt(split[0]);
            if (split.length > 1) {
                mMessage = split[1];
            }
            mData = bodyData;
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
         * @return the body data
         * @since 0.5.0
         */
        public byte[] getData() {
            return mData;
        }

        /**
         * Assign the body data of the Response.
         *
         * @param data the body data
         * @since 0.5.0
         */
        public void setData(byte[] data) {
            mData = data;
        }
    }

    /**
     * An AbortResponse represents the deliberate termination of an action.
     */
    public class AbortResponse extends Response {
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
