package co.blustor.gatekeepersdk.devices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOCommPort implements CommPort {

    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public IOCommPort(InputStream inputStream, OutputStream outputStream) {
        mInputStream = inputStream;
        mOutputStream = outputStream;
    }

    @Override
    public int read(byte[] data) throws IOException {
        int totalBytesRead = 0;
        int bytesRead = 0;
        while (totalBytesRead < data.length && bytesRead != -1) {
            bytesRead = mInputStream.read(data, totalBytesRead, data.length - totalBytesRead);
            if (bytesRead != -1) {
                totalBytesRead += bytesRead;
            }
        }
        return totalBytesRead;
    }

    @Override
    public void write(byte[] data) throws IOException {
        mOutputStream.write(data);
    }

    @Override
    public void close() throws IOException {
        mInputStream.close();
        mOutputStream.close();
    }
}
