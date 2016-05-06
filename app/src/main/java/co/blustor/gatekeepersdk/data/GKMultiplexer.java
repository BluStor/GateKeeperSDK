package co.blustor.gatekeepersdk.data;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Intended for internal use only.
 */
public class GKMultiplexer {
    public static final String TAG = GKMultiplexer.class.getSimpleName();
    public static final int MAXIMUM_PAYLOAD_SIZE = 512;
    public static final int COMMAND_CHANNEL = 1;
    public static final int DATA_CHANNEL = 2;

    private static final byte CARRIAGE_RETURN = 13;
    private static final byte LINE_FEED = 10;
    private static final int UPLOAD_DELAY_MILLIS = 1;

    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public GKMultiplexer(InputStream inputStream, OutputStream outputStream) {
        mInputStream = inputStream;
        mOutputStream = outputStream;
    }

    public void writeToCommandChannel(byte[] data) throws IOException {
        write(data, COMMAND_CHANNEL);
    }

    public void writeToDataChannel(byte[] data) throws IOException {
        write(data, DATA_CHANNEL);
    }

    public void writeToDataChannel(InputStream inputStream) throws IOException, InterruptedException {
        int bytesRead;
        byte[] buffer = new byte[MAXIMUM_PAYLOAD_SIZE];
        do {
            bytesRead = inputStream.read(buffer, 0, buffer.length);
            if (bytesRead == -1) {
                continue;
            }
            if (bytesRead < buffer.length) {
                writeToDataChannel(Arrays.copyOf(buffer, bytesRead));
            } else {
                writeToDataChannel(buffer);
            }
            Thread.sleep(UPLOAD_DELAY_MILLIS);
        } while (bytesRead != -1);
    }

    public byte[] readCommandChannelLine() throws IOException {
        DataPacket packet = DataPacket.Builder.build(mInputStream);
        return readCommandLine(packet);
    }

    /**
     * Read from the data channel and write it to the specified file. Once a response is read from
     * the command channel, we are done reading data and can return the command response
     *
     * @param dataFile the {@code File} to write the data channel to
     * @return the data read from the command channel following data transfer
     * @throws IOException when connection to the card is disrupted or writing data to a file fails
     */
    public byte[] readDataChannelToFile(File dataFile) throws IOException {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(dataFile);
            DataPacket packet = DataPacket.Builder.build(mInputStream);

            while (DATA_CHANNEL == packet.getChannel()) {
                byte[] payload = packet.getPayload();
                fileOutputStream.write(payload);
                packet = DataPacket.Builder.build(mInputStream);
            }

            return readCommandLine(packet);
        } catch (IOException e) {
            Log.e(TAG, "Exception occurred while buffering a DataPacket", e);
            cleanup();
            throw e;
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    public void cleanup() {
        try {
            mInputStream.close();
            mOutputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception occurred during cleanup", e);
        }
    }

    private byte[] readCommandLine(DataPacket packet) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        while (!containsCRLF(packet.getPayload())) {
            copyUntilCRLF(packet.getPayload(), bytes);
            packet = DataPacket.Builder.build(mInputStream);
        }
        copyUntilCRLF(packet.getPayload(), bytes);
        return bytes.toByteArray();
    }

    private boolean containsCRLF(byte[] payload) {
        for (int i = 0; i < payload.length - 1; i++) {
            if (payload[i] == CARRIAGE_RETURN && payload[i + 1] == LINE_FEED) {
                return true;
            }
        }
        return false;
    }

    private byte[] copyUntilCRLF(byte[] data, ByteArrayOutputStream bytes) throws IOException {
        for (int i = 0; i < data.length; i++) {
            byte a = data[i];
            if (a == CARRIAGE_RETURN && i < data.length - 1 && data[i + 1] == LINE_FEED) {
                break;
            }
            bytes.write(a);
        }
        return bytes.toByteArray();
    }

    private void write(byte[] data, int channel) throws IOException {
        byte[] packetBytes = DataPacket.Builder.toPacketBytes(data, channel);
        mOutputStream.write(packetBytes);
    }
}
