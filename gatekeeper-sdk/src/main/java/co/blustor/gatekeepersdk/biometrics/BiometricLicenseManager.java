package co.blustor.gatekeepersdk.biometrics;

import android.util.Log;

import com.neurotec.licensing.NLicense;
import co.blustor.gatekeepersdk.utils.GKFileUtils;

import java.io.IOException;

/**
 * Intended for internal use only.
 */
public class BiometricLicenseManager {

    private static final String TAG = BiometricLicenseManager.class.getCanonicalName();

    private String mHost;
    private int mPort;

    public BiometricLicenseManager(String host, int port) {
        Log.d(TAG, "BiometricLicenseManager(): host = " + host + ", port = " + port);
        mHost = host;
        mPort = port;
    }

    public void add(String license) throws IOException {
        Log.d(TAG, "add(): license = " + license);
        NLicense.add(license);
    }

    public boolean obtainComponents(String components) throws IOException {
        return NLicense.obtainComponents(mHost, mPort, components);
    }

    public String generateID(String serialNumber) throws IOException {
        Log.d(TAG, "generateID(): serialNumber = " + serialNumber);
        return NLicense.generateID(serialNumber);
    }

    public String activateOnline(String id) throws IOException {
        Log.d(TAG, "activateOnline(): id = " + id);
        return NLicense.activateOnline(id);
    }

    public void deactivateOnline(String license) throws IOException {
        Log.d(TAG, "deactivateOnline(): license = " + license);
        NLicense.deactivateOnline(license);
    }
}
