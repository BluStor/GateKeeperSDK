package co.blustor.gatekeepersdk.biometrics.licensing;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import co.blustor.gatekeepersdk.data.GKFile;
import co.blustor.gatekeepersdk.services.GKFileActions;
import co.blustor.gatekeepersdk.utils.GKFileUtils;

/**
 * Intended for internal use only.
 */
public class FetchExistingLicense {

    public static final String TAG = FetchExistingLicense.class.getCanonicalName();

    private GKFileActions mFileActions;

    public FetchExistingLicense(GKFileActions fileActions) {
        mFileActions = fileActions;
    }

    public String execute(GKFile licenseFile) throws IOException {
        Log.d(TAG, "execute(): licenseFile.getCardPath() = " + licenseFile.getCardPath());
        Log.d(TAG, "execute(): licenseFile.getName() = " + licenseFile.getName());
        File tempLicenseFile = File.createTempFile(licenseFile.getName(), licenseFile.getExtension());
        // File tempLicenseFile = new File(licenseFile.getName());
        if (tempLicenseFile !=null) {
            Log.d(TAG, "execute(): tempLicenseFile = " + tempLicenseFile.getName());
        } else {
            Log.d(TAG, "execute(): tempLicenseFile = null");
        }
        GKFileActions.GetFileResult getLicenseFileResult = mFileActions.getFile(licenseFile, tempLicenseFile);

        if (getLicenseFileResult.getStatus() != GKFileActions.Status.SUCCESS) {
            throw new IOException("Could not retrieve license file contents");
        }

        return GKFileUtils.readFile(getLicenseFileResult.getFile());
    }

}