package co.blustor.gatekeepersdk.devices;

import android.content.Context;

import java.io.IOException;

import co.blustor.gatekeepersdk.data.GKMultiplexer;

public class GKUsbCard extends GKCard {

    private Context mContext;
    private UsbCommPort mCommPort;

    public GKUsbCard(Context context) {
        super();
        mContext = context;
    }

    @Override
    protected GKMultiplexer connectToMultiplexer() throws IOException {
        mCommPort = new UsbCommPort(mContext);
        mCommPort.connect();
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
