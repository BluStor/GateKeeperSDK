package co.blustor.gatekeepersdk.biometrics;

import com.neurotec.licensing.NLicense;
import co.blustor.gatekeepersdk.utils.GKFileUtils;

import java.io.IOException;

/**
 * Intended for internal use only.
 */
public class BiometricLicenseManager {

    private static final String TAG = GKFileUtils.DEBUG_PREFIX + BiometricLicenseManager.class.getSimpleName();

    private String mHost;
    private int mPort;

    public BiometricLicenseManager(String host, int port) {
        mHost = host;
        mPort = port;
    }

    public void add(String license) throws IOException {
        NLicense.add(license);
    }

    public boolean obtainComponents(String components) throws IOException {
        return NLicense.obtainComponents(mHost, mPort, components);
    }

    public String generateID(String serialNumber) throws IOException {
        return NLicense.generateID(serialNumber);
    }

    public String activateOnline(String id) throws IOException {
        return NLicense.activateOnline(id);
    }

    public void deactivateOnline(String license) throws IOException {
        NLicense.deactivateOnline(license);
    }
}
