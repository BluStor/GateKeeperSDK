package co.blustor.gatekeepersdk.biometrics;

import android.util.Log;

import java.io.IOException;

import co.blustor.gatekeepersdk.biometrics.licensing.FetchExistingLicense;
import co.blustor.gatekeepersdk.biometrics.licensing.GenerateActiveLicense;
import co.blustor.gatekeepersdk.biometrics.licensing.LicenseFileExtensions;
import co.blustor.gatekeepersdk.data.GKFile;
import co.blustor.gatekeepersdk.services.GKFileActions;
import co.blustor.gatekeepersdk.utils.GKConst;
import co.blustor.gatekeepersdk.utils.GKFileUtils;

/**
 * GKLicensing is responsible for obtaining the licenses necessary for GateKeeper biometrics.
 */
public class GKLicensing {
    private static final String TAG = GKLicensing.class.getCanonicalName();

    /**
     * The {@code String} license IDs needed by GateKeeper biometrics.
     */
    public static final String[] LICENSES = {
            "Biometrics.FaceExtraction",
            "Biometrics.FaceDetection",
            "Devices.Cameras"
    };

    protected final String mLicenseSubDir;
    private final GKFileActions mFileActions;
    private final BiometricLicenseManager mLicenseManager;
    protected FetchExistingLicense mFetchExistingLicense;
    protected GenerateActiveLicense mGenerateActiveLicense;

    /**
     * Create a {@code GKLicensing} with the given host address and port.
     *
     * @param licenseSubDir  the directory on the card to look for activated licenses
     * @param fileActions    {@code GKFileActions}
     * @param licenseManager a BiometricLicenseManager which wraps the NLicense class
     * @since 0.11.0
     */
    public GKLicensing(String licenseSubDir, GKFileActions fileActions, BiometricLicenseManager licenseManager) {
        mLicenseSubDir = parseLicenseSubDir(licenseSubDir);
        mFileActions = fileActions;
        mLicenseManager = licenseManager;
    }

    /**
     * Checks the specified subdirectory for an activated license and validates it
     * If no activated license is found, attempt to activate one
     *
     * @return a {@code GKLicenseValidationResult} indicating success or a reason for failure
     * @since 0.11.0
     */
    public GKLicenseValidationResult obtainLicenses() {
        Log.d(TAG, "obtainLicenses() Get the license file");
        try {
            String license = getLicense();
            if (license == null) {
                Log.d(TAG, "obtainLicenses(): return NO_LICENSES_AVAILABLE");
                return GKLicenseValidationResult.NO_LICENSES_AVAILABLE;
            } else {
                Log.d(TAG, "obtainLicenses(): Validate license");
                return validateLicense(license);
            }
        } catch (IOException e1) {
            Log.e(TAG, e1.getMessage());
            return GKLicenseValidationResult.COMMUNICATION_ERROR;
        } catch (Exception e2) {
            Log.e(TAG, e2.getMessage());
            return GKLicenseValidationResult.ERROR;
        }
    }

    private String getLicense() throws IOException {
        Log.d(TAG, "getLicense(): mLicenseSubDir = " + mLicenseSubDir);
        String licenseSubDir = GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, mLicenseSubDir);
        GKFile existingLicenseFile = getFirstFile(licenseSubDir, LicenseFileExtensions.LICENSE);
        if (existingLicenseFile != null) {
            return getFetchExistingLicense().execute(existingLicenseFile);
        }

        GKFile serialNumberFile = getFirstFile(GKFileUtils.LICENSE_ROOT, LicenseFileExtensions.SERIAL_NUMBER);
        if (serialNumberFile != null) {
            return getGenerateActiveLicense().execute(serialNumberFile);
        }

        return null;
    }

    private GKLicenseValidationResult validateLicense(String license) throws IOException {
        Log.d(TAG, "validateLicense(): license = " + license);

        mLicenseManager.add(license);

        for (String component : LICENSES) {
            Log.d(TAG, "validateLicense(): component = " + component);
            if (!mLicenseManager.obtainComponents(component)) {
                Log.d(TAG, "validateLicense(): return GKLicenseValidationResult.VALIDATION_FAILURE");
                return GKLicenseValidationResult.VALIDATION_FAILURE;
            }
        }
        Log.d(TAG, "validateLicense(): return GKLicenseValidationResult.SUCCESS");
        return GKLicenseValidationResult.SUCCESS;
    }

    private GKFile getFirstFile(String dir, String extension) throws IOException {
        Log.d(TAG, "getFirstFile(): dir = " + dir + ", extension = " + extension);
        GKFileActions.ListFilesResult listFilesResult = mFileActions.listFiles(dir);

        for (GKFile file : listFilesResult.getFiles()) {
            if (extension.equals(file.getExtension())) {
                Log.d(TAG, "getFirstFile(): File match = " + file.getName());
                return file;
            }
        }
        Log.d(TAG, "getFirstFile(): No matching file, return null");
        return null;
    }

    private FetchExistingLicense getFetchExistingLicense() {
        Log.d(TAG, "getFetchExistingLicense()");
        if (mFetchExistingLicense == null) {
            Log.d(TAG, "mFetchExistingLicense = null.  Create new one");
            mFetchExistingLicense = new FetchExistingLicense(mFileActions);
        }
        return mFetchExistingLicense;
    }

    private GenerateActiveLicense getGenerateActiveLicense() {
        Log.d(TAG, "getGenerateActiveLicense()");
        if (mGenerateActiveLicense == null) {
            mGenerateActiveLicense = new GenerateActiveLicense(mLicenseManager, mFileActions, mLicenseSubDir);
        }
        return mGenerateActiveLicense;
    }

    private String parseLicenseSubDir(String licenseSubDir) {
        return licenseSubDir.replaceAll("[^A-Za-z0-9\\._-]", "_");
    }
}
