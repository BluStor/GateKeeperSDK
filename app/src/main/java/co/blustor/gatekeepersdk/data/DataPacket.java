package co.blustor.gatekeepersdk.data;

import java.io.IOException;
import java.io.InputStream;

public class DataPacket {
    public static final int HEADER_SIZE = 3;
    public static final int CHECKSUM_SIZE = 2;

    public static final byte MOST_SIGNIFICANT_BIT = 0x00;
    public static final byte LEAST_SIGNIFICANT_BIT = 0x00;

    private byte[] mPayload;
    private int mChannel;

    public DataPacket(byte[] payload, int channel) {
        mPayload = payload;
        mChannel = channel;
    }

    public byte[] getPayload() {
        return mPayload;
    }

    public int getChannel() {
        return mChannel;
    }

    public static class Builder {
        public static DataPacket build(InputStream inputStream) throws IOException {
            byte[] header = readHeader(inputStream);
            int packetSize = getPacketSize(header);
            int channel = getPacketChannel(header);
            byte[] payload = readPayload(inputStream, packetSize);
            byte[] checksum = readChecksum(inputStream);

            return new DataPacket(payload, channel);
        }

        public static byte[] toPacketBytes(byte[] data, int channel) {
            int packetSize = data.length + 5;
            byte channelByte = getChannelByte(channel);
            byte msb = getMSB(packetSize);
            byte lsb = getLSB(packetSize);

            byte[] packet = new byte[data.length + 5];
            packet[0] = channelByte;
            packet[1] = msb;
            packet[2] = lsb;
            for (int i = 0; i < data.length; i++) {
                packet[i + 3] = data[i];
            }

            packet[packet.length - 2] = DataPacket.MOST_SIGNIFICANT_BIT;
            packet[packet.length - 1] = DataPacket.LEAST_SIGNIFICANT_BIT;
            return packet;
        }

        private static int getPacketSize(byte[] header) {
            byte packetSizeMSB = header[1];
            byte packetSizeLSB = header[2];
            int packetSize = (int) packetSizeMSB << 8;
            packetSize += (int) packetSizeLSB & 0xFF;
            return packetSize;
        }

        private static int getPacketChannel(byte[] header) {
            return (int) header[0];
        }

        private static byte[] readHeader(InputStream inputStream) throws IOException {
            return fillByteArrayFromStream(inputStream, DataPacket.HEADER_SIZE);
        }

        private static byte[] readPayload(InputStream inputStream, int packetSize) throws IOException {
            int payloadsize = packetSize - (DataPacket.HEADER_SIZE + DataPacket.CHECKSUM_SIZE);
            return fillByteArrayFromStream(inputStream, payloadsize);
        }

        private static byte[] readChecksum(InputStream inputStream) throws IOException {
            return fillByteArrayFromStream(inputStream, DataPacket.CHECKSUM_SIZE);
        }

        private static byte[] fillByteArrayFromStream(InputStream inputStream, int length) throws IOException {
            byte[] data = new byte[length];
            int totalBytesRead = 0;
            int bytesRead = 0;
            while (totalBytesRead < length && bytesRead != -1) {
                bytesRead = inputStream.read(data, totalBytesRead, length - totalBytesRead);
                if (bytesRead != -1) {
                    totalBytesRead += bytesRead;
                }
            }
            return data;
        }

        private static byte getChannelByte(int channel) {
            return (byte) (channel & 0xff);
        }

        private static byte getMSB(int size) {
            return (byte) (size >> 8);
        }

        private static byte getLSB(int size) {
            return (byte) (size & 0xff);
        }
    }
}
