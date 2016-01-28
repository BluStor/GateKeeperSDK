package co.blustor.gatekeeper.biometrics;

import com.neurotec.licensing.NLicense;

import java.io.IOException;

/**
 * GKLicensing is responsible for obtaining the licenses necessary for GateKeeper biometrics.
 */
public class GKLicensing {
    public static final String TAG = GKLicensing.class.getSimpleName();

    /**
     * The {@code String} license IDs needed by GateKeeper biometrics.
     */
    public static final String[] LICENSES = {
            "Biometrics.FaceExtraction",
            "Biometrics.FaceDetection",
            "Devices.Cameras"
    };

    private final String sHostAddress;
    private final int sHostPort;

    /**
     * Create a {@code GKLicensing} with the given host address and port.
     *
     * @param hostAddress
     * @param hostPort
     * @since 0.5.0
     */
    public GKLicensing(String hostAddress, int hostPort) {
        sHostAddress = hostAddress;
        sHostPort = hostPort;
    }

    /**
     * Obtain the licenses, making a network call if necessary.
     *
     * @since 0.5.0
     */
    public void obtainLicenses() {
        for (String component : LICENSES) {
            try {
                NLicense.obtainComponents(sHostAddress, sHostPort, component);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("licenses were not obtained");
            }
        }
    }
}
