package co.blustor.gatekeepersdk.devices;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

public class UsbCommPort implements CommPort {
    private static final int DEFAULT_TIMEOUT_MILLIS = 300;
    private Context mContext;
    private UsbSerialPort mPort;
    private boolean mIsClosed = true;

    public UsbCommPort(Context context) {
        mContext = context;
    }

    public void connect() throws IOException {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            manager.requestPermission(driver.getDevice(), );
            return;
        }

        mPort = driver.getPorts().get(0);
        mPort.open(connection);
        mIsClosed = false;
//        mPort.setBaudRate(115200);
    }

    @Override
    public int read(byte[] data) throws IOException {
        return mPort.read(data, DEFAULT_TIMEOUT_MILLIS);
    }

    @Override
    public void write(byte[] data) throws IOException {
        mPort.write(data, DEFAULT_TIMEOUT_MILLIS);
    }

    @Override
    public void close() throws IOException {
        mIsClosed = true;
        mPort.close();
    }

    public boolean isDisconnected() {
        return mIsClosed;
    }
}
