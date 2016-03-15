package co.blustor.gatekeepersdk.biometrics;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.blustor.gatekeepersdk.biometrics.licensing.FetchExistingLicense;
import co.blustor.gatekeepersdk.biometrics.licensing.GenerateActiveLicense;
import co.blustor.gatekeepersdk.data.GKFile;
import co.blustor.gatekeepersdk.services.GKFileActions;
import co.blustor.gatekeepersdk.utils.GKFileUtils;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GKLicensingTest {
    private static final String licenseSubdir = "licensesubdir";
    private static final String licenseContents = "test\nlicense\n";
    private GKLicensing licensing;
    private FetchExistingLicense fetchExistingLicense;
    private GenerateActiveLicense generateActiveLicense;
    private GKFileActions fileActions;
    private BiometricLicenseManager licenseManager;
    private GKFileActions.ListFilesResult listFilesResultWithLicense;
    private GKFile licenseFile;
    private GKFileActions.ListFilesResult listFilesResultWithSerial;
    private GKFile serialFile;

    @Before
    public void setUp() throws IOException {
        fileActions = mock(GKFileActions.class);
        licenseManager = mock(BiometricLicenseManager.class);
        fetchExistingLicense = mock(FetchExistingLicense.class);
        generateActiveLicense = mock(GenerateActiveLicense.class);
        licensing = new GKLicensing(licenseSubdir, fileActions, licenseManager);
        licensing.mFetchExistingLicense = fetchExistingLicense;
        licensing.mGenerateActiveLicense = generateActiveLicense;

        listFilesResultWithLicense = mock(GKFileActions.ListFilesResult.class);
        licenseFile = new GKFile("test.lic", GKFile.Type.FILE);
        List<GKFile> licenseFiles = new ArrayList<>();
        licenseFiles.add(licenseFile);
        when(listFilesResultWithLicense.getFiles()).thenReturn(licenseFiles);

        listFilesResultWithSerial = mock(GKFileActions.ListFilesResult.class);
        serialFile = new GKFile("test.sn", GKFile.Type.FILE);
        List<GKFile> serialFiles = new ArrayList<>();
        serialFiles.add(serialFile);
        when(listFilesResultWithSerial.getFiles()).thenReturn(serialFiles);
    }

    @Test
    public void replacesAllIllegalDirectoryNameCharactersWithUnderscores() {
        GKLicensing licensing = new GKLicensing("illegal&characters with:spaces%and+stuff", fileActions, licenseManager);
        assertThat(licensing.mLicenseSubDir, is(equalTo("illegal_characters_with_spaces_and_stuff")));
    }

    @Test
    public void returnsErrorIfListFilesInTheLicenseSubDirThrowsException() throws Exception {
        when(fileActions.listFiles(GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, licenseSubdir))).thenThrow(IOException.class);

        GKLicenseValidationResult result = licensing.obtainLicenses();

        assertThat(result, is(GKLicenseValidationResult.ERROR));
    }

    @Test
    public void returnsErrorIfFetchExistingLicenseThrowsException() throws Exception {
        when(fileActions.listFiles(GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, licenseSubdir))).thenReturn(listFilesResultWithLicense);
        when(fetchExistingLicense.execute(licenseFile)).thenThrow(IOException.class);

        GKLicenseValidationResult result = licensing.obtainLicenses();

        assertThat(result, is(GKLicenseValidationResult.ERROR));
    }

    @Test
    public void returnsValidationFailureIfAnyExistingLicenseComponentsCannotBeObtained() throws Exception {
        when(fileActions.listFiles(GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, licenseSubdir))).thenReturn(listFilesResultWithLicense);
        when(fetchExistingLicense.execute(licenseFile)).thenReturn(licenseContents);
        when(licenseManager.obtainComponents(anyString())).thenReturn(true);
        when(licenseManager.obtainComponents("Biometrics.FaceDetection")).thenReturn(false);

        GKLicenseValidationResult result = licensing.obtainLicenses();

        verify(licenseManager).add(licenseContents);
        assertThat(result, is(GKLicenseValidationResult.VALIDATION_FAILURE));
    }

    @Test
    public void returnsSuccessIfAllExistingLicenseComponentsCanBeObtained() throws Exception {
        when(fileActions.listFiles(GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, licenseSubdir))).thenReturn(listFilesResultWithLicense);
        when(fetchExistingLicense.execute(licenseFile)).thenReturn(licenseContents);
        when(licenseManager.obtainComponents(anyString())).thenReturn(true);

        GKLicenseValidationResult result = licensing.obtainLicenses();

        verify(licenseManager).add(licenseContents);
        assertThat(result, is(GKLicenseValidationResult.SUCCESS));
    }

    @Test
    public void returnsErrorIfLicenseManagerCallsThrowAnException() throws Exception {
        when(fileActions.listFiles(GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, licenseSubdir))).thenReturn(listFilesResultWithLicense);
        when(fetchExistingLicense.execute(licenseFile)).thenReturn(licenseContents);
        when(licenseManager.obtainComponents(anyString())).thenThrow(IOException.class);

        GKLicenseValidationResult result = licensing.obtainLicenses();

        assertThat(result, is(GKLicenseValidationResult.ERROR));
    }

    @Test
    public void returnsErrorIfNoActiveLicensesAndListFilesThrowsException() throws Exception {
        stubNoActiveLicenses();
        when(fileActions.listFiles(GKFileUtils.LICENSE_ROOT)).thenThrow(IOException.class);

        GKLicenseValidationResult result = licensing.obtainLicenses();

        assertThat(result, is(GKLicenseValidationResult.ERROR));
    }

    @Test
    public void returnsNoLicensesAvailableIfNoLicenseFilesCanBeFoundOnTheCard() throws Exception {
        stubNoActiveLicenses();
        GKFileActions.ListFilesResult listResult = mock(GKFileActions.ListFilesResult.class);
        List<GKFile> files = new ArrayList<>();
        GKFile nonLicenseFile = new GKFile("test.GIF", GKFile.Type.FILE);
        files.add(nonLicenseFile);
        when(listResult.getFiles()).thenReturn(files);
        when(fileActions.listFiles(GKFileUtils.LICENSE_ROOT)).thenReturn(listResult);

        GKLicenseValidationResult result = licensing.obtainLicenses();

        assertThat(result, is(GKLicenseValidationResult.NO_LICENSES_AVAILABLE));
    }

    @Test
    public void returnsErrorIfNoActiveLicensesAndGenerateActiveLicenseThrowsException() throws Exception {
        stubNoActiveLicenses();
        when(fileActions.listFiles(GKFileUtils.LICENSE_ROOT)).thenReturn(listFilesResultWithSerial);
        when(generateActiveLicense.execute(serialFile)).thenThrow(IOException.class);

        GKLicenseValidationResult result = licensing.obtainLicenses();

        assertThat(result, is(GKLicenseValidationResult.ERROR));
    }

    @Test
    public void returnsValidationFailureIfGeneratedLicenseComponentsCannotBeObtained() throws Exception {
        stubNoActiveLicenses();
        when(fileActions.listFiles(GKFileUtils.LICENSE_ROOT)).thenReturn(listFilesResultWithSerial);
        when(generateActiveLicense.execute(serialFile)).thenReturn(licenseContents);

        when(licenseManager.obtainComponents(anyString())).thenReturn(true);
        when(licenseManager.obtainComponents("Biometrics.FaceDetection")).thenReturn(false);

        GKLicenseValidationResult result = licensing.obtainLicenses();

        verify(licenseManager).add(licenseContents);
        assertThat(result, is(GKLicenseValidationResult.VALIDATION_FAILURE));
    }

    @Test
    public void returnsSuccessIfAllLicenseComponentsCanBeObtainedFromNewlyCreatedLicense() throws Exception {
        stubNoActiveLicenses();
        when(fileActions.listFiles(GKFileUtils.LICENSE_ROOT)).thenReturn(listFilesResultWithSerial);
        when(generateActiveLicense.execute(serialFile)).thenReturn(licenseContents);
        when(licenseManager.obtainComponents(anyString())).thenReturn(true);

        GKLicenseValidationResult result = licensing.obtainLicenses();

        verify(licenseManager).add(licenseContents);
        assertThat(result, is(GKLicenseValidationResult.SUCCESS));
    }

    private void stubNoActiveLicenses() throws Exception {
        GKFileActions.ListFilesResult listResult = mock(GKFileActions.ListFilesResult.class);
        when(listResult.getFiles()).thenReturn(new ArrayList<GKFile>());
        when(fileActions.listFiles(GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, licenseSubdir))).thenReturn(listResult);
    }
}
