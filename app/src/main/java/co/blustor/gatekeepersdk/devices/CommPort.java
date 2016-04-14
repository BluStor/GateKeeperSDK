package co.blustor.gatekeepersdk.devices;

import java.io.IOException;

public interface CommPort {
    int read(byte[] data) throws IOException;
    void write(byte[] data) throws IOException;
    void close() throws IOException;
}
