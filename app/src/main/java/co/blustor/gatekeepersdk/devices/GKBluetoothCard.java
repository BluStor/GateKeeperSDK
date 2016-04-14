package co.blustor.gatekeepersdk.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import co.blustor.gatekeepersdk.data.GKMultiplexer;

public class GKBluetoothCard extends GKCard {
    public static final String TAG = GKBluetoothCard.class.getSimpleName();

    private static final UUID BLUETOOTH_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final String mCardName;
    private BluetoothSocket mBluetoothSocket;

    /**
     * Create a {@code GKBluetoothCard} to connect with the GateKeeper Card having the
     * given name.
     *
     * @param cardName the Bluetooth pairing name of the GateKeeper Card
     */
    public GKBluetoothCard(String cardName) {
        super();
        mCardName = cardName;
    }

    @Override
    protected GKMultiplexer connectToMultiplexer() throws IOException {
        BluetoothDevice bluetoothDevice = findBluetoothDevice();
        if (bluetoothDevice == null) {
            throw new IOException("Bluetooth device not found");
        }
        mBluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID);
        mBluetoothSocket.connect();

        IOCommPort commPort = new IOCommPort(mBluetoothSocket.getInputStream(), mBluetoothSocket.getOutputStream());
        return new GKMultiplexer(commPort);
    }

    @Override
    public void disconnect() throws IOException {
        if (mBluetoothSocket != null) {
            mBluetoothSocket.close();
        }
        super.disconnect();
    }

    @Override
    protected boolean isDisconnected() {
        return mMultiplexer == null || mBluetoothSocket == null || !mBluetoothSocket.isConnected();
    }

    @Nullable
    private BluetoothDevice findBluetoothDevice() {
        BluetoothAdapter adapter = getBluetoothAdapter();
        if (!adapter.isEnabled()) {
            onConnectionChanged(ConnectionState.BLUETOOTH_DISABLED);
            return null;
        }
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(mCardName)) {
                return device;
            }
        }
        onConnectionChanged(ConnectionState.CARD_NOT_PAIRED);
        return null;
    }

    @NonNull
    private BluetoothAdapter getBluetoothAdapter() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new RuntimeException("Bluetooth is not available on this device");
        }
        return adapter;
    }
}
