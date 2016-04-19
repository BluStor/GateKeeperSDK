package co.blustor.gatekeepersdk.devices;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

public class UsbCommPort implements CommPort {
    private static final int DEFAULT_TIMEOUT_MILLIS = 1000;
    private static final String ACTION_USB_PERMISSION = "co.blustor.gatekeepersdk.GKUsbCard";
    private Context mContext;
    private UsbSerialPort mPort;
    private boolean mIsClosed = true;
    private BroadcastReceiver mBroadcastReceiver;
    private UsbSerialDriver mDriver;
    private UsbManager mUsbManager;
    private UsbListener mListener;

    public UsbCommPort(Context context) {
        mContext = context;
        mListener = (UsbListener) context;
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (!availableDrivers.isEmpty()) {
            mDriver = availableDrivers.get(0);
        }
        UsbDeviceConnection connection = mUsbManager.openDevice(mDriver.getDevice());
        if (connection == null) {
            mContext.registerReceiver(getBroadcastReceiver(), new IntentFilter(ACTION_USB_PERMISSION));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
            mUsbManager.requestPermission(mDriver.getDevice(), pendingIntent);
        }
    }

    @Override
    public int read(byte[] data) throws IOException {
            try {
                connect();
                Thread.sleep(2000);
                Log.d("WATWAT", "reading");
                return mPort.read(data, DEFAULT_TIMEOUT_MILLIS);
            } catch (IOException e) {
                Log.e("WATWAT", "error reading", e);
                throw e;
            } catch (InterruptedException e) {
                Log.e("WATWAT", "no sleep for you", e);
                throw new IOException("WAT IS THIS");
            }
    }

    @Override
    public void write(byte[] data) throws IOException {
        synchronized (this) {
            try {
                connect();
                Log.d("WATWAT", "writing");
                mPort.write(data, DEFAULT_TIMEOUT_MILLIS);
            } catch (IOException e) {
                Log.e("WATWAT", "error writing", e);
                throw e;
            }
        }
    }

    @Override
    public void close() throws IOException {
        mIsClosed = true;
        mPort.close();
    }

    public boolean isDisconnected() {
        return mIsClosed;
    }

    private void connect() throws IOException {
        synchronized (this) {
            try {
                Log.d("WATWAT", "connecting");
                UsbDeviceConnection connection = mUsbManager.openDevice(mDriver.getDevice());
                mPort = mDriver.getPorts().get(0);
                mPort.open(connection);
                mPort.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                Log.e("WATWAT", "error connecting", e);
            }
            mIsClosed = false;
        }
    }

    private BroadcastReceiver getBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (ACTION_USB_PERMISSION.equals(action)) {
                        synchronized (this) {
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                mContext.unregisterReceiver(getBroadcastReceiver());
                                mListener.onUsbConnected();
                            }
                        }
                    }
                }
            };
        }
        return mBroadcastReceiver;
    }

    public interface UsbListener {
        void onUsbConnected();
    }
}
