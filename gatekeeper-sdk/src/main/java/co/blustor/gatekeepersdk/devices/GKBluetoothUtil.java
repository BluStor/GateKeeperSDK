package co.blustor.gatekeepersdk.devices;

import android.bluetooth.BluetoothAdapter;

public class GKBluetoothUtil {
    public GKBluetoothUtil() { }

    public void cycleBluetoothAdaptor() {
        // Some Android devices BT adapters enter into a bad state
        // and re-cycling the adapter is an ugly fix that seems to work
        BluetoothAdapter.getDefaultAdapter().disable();
        while (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
        }
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }
        BluetoothAdapter.getDefaultAdapter().enable();
        while (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
        }
    }
}