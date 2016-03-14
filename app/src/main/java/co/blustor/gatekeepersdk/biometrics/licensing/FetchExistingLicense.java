package co.blustor.gatekeepersdk.biometrics.licensing;

import java.io.File;
import java.io.IOException;

import co.blustor.gatekeepersdk.data.GKFile;
import co.blustor.gatekeepersdk.services.GKFileActions;
import co.blustor.gatekeepersdk.utils.GKFileUtils;

public class FetchExistingLicense {

    private GKFileActions mFileActions;

    public FetchExistingLicense(GKFileActions fileActions) {
        mFileActions = fileActions;
    }

    public String execute(GKFile licenseFile) throws IOException {
        File tempLicenseFile = File.createTempFile(licenseFile.getName(), licenseFile.getExtension());
        GKFileActions.GetFileResult getLicenseFileResult = mFileActions.getFile(licenseFile, tempLicenseFile);

        if (getLicenseFileResult.getStatus() != GKFileActions.Status.SUCCESS) {
            throw new IOException("Could not retrieve license file contents");
        }

        return GKFileUtils.readFile(getLicenseFileResult.getFile());
    }

}