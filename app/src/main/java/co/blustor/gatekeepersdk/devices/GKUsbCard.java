package co.blustor.gatekeepersdk.devices;

import java.io.IOException;

import co.blustor.gatekeepersdk.data.GKMultiplexer;

public class GKUsbCard extends GKCard {

    private UsbCommPort mCommPort;

    public GKUsbCard(UsbCommPort commPort) {
        super();
        mCommPort = commPort;
    }

    @Override
    protected GKMultiplexer connectToMultiplexer() throws IOException {
        return new GKMultiplexer(mCommPort);
    }

    @Override
    protected boolean isDisconnected() {
        return mMultiplexer == null || mCommPort == null || mCommPort.isDisconnected();
    }

    @Override
    public void disconnect() throws IOException {
        if (mCommPort != null) {
            mCommPort.close();
        }
        super.disconnect();
    }
}
