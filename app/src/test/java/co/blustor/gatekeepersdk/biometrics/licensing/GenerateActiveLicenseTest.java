package co.blustor.gatekeepersdk.biometrics.licensing;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.io.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import co.blustor.gatekeepersdk.biometrics.BiometricLicenseManager;
import co.blustor.gatekeepersdk.data.GKFile;
import co.blustor.gatekeepersdk.services.GKFileActions;
import co.blustor.gatekeepersdk.utils.GKFileUtils;
import co.blustor.gatekeepersdk.utils.GKStringUtils;

import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class GenerateActiveLicenseTest {
    private static final String licenseSubdir = "licensesubdir";
    private static final String idContents = "test\nid\n";
    private static final String licenseContents = "test\nlicense\n";
    private static final String serialContents = "test\nserial\nnumber\n";
    private GenerateActiveLicense subject;
    private GKFileActions fileActions;
    private BiometricLicenseManager licenseManager;
    private File tempSerialNumberFile;
    private GKFile serialFile;
    private GKFileActions.GetFileResult successfulGetFileResultForSerial;

    @Before
    public void setUp() throws IOException {
        fileActions = mock(GKFileActions.class);
        licenseManager = mock(BiometricLicenseManager.class);
        subject = new GenerateActiveLicense(licenseManager, fileActions, licenseSubdir);

        tempSerialNumberFile = File.createTempFile("test-serial", LicenseFileExtensions.SERIAL_NUMBER);
        FileWriter serialWriter = new FileWriter(tempSerialNumberFile);
        serialWriter.write(serialContents);
        serialWriter.close();

        serialFile = new GKFile("test.sn", GKFile.Type.FILE);

        successfulGetFileResultForSerial = mock(GKFileActions.GetFileResult.class);
        when(successfulGetFileResultForSerial.getStatus()).thenReturn(GKFileActions.Status.SUCCESS);
        when(successfulGetFileResultForSerial.getFile()).thenReturn(tempSerialNumberFile);

        GKFileActions.PutFileResult putFileResult = mock(GKFileActions.PutFileResult.class);
        when(putFileResult.getStatus()).thenReturn(GKFileActions.Status.SUCCESS);
        when(fileActions.putFile(any(ByteArrayInputStream.class), anyString())).thenReturn(putFileResult);

        GKFileActions.FileResult deleteFileResult = mock(GKFileActions.FileResult.class);
        when(deleteFileResult.getStatus()).thenReturn(GKFileActions.Status.SUCCESS);
        when(fileActions.deleteFile(serialFile)).thenReturn(deleteFileResult);

        GKFileActions.ListFilesResult listResult = mock(GKFileActions.ListFilesResult.class);
        when(listResult.getStatus()).thenReturn(GKFileActions.Status.SUCCESS);
        when(listResult.getFiles()).thenReturn(new ArrayList<GKFile>());
        when(fileActions.listFiles(GKFileUtils.LICENSE_ROOT)).thenReturn(listResult);

        GKFileActions.FileResult mkdirResult = mock(GKFileActions.FileResult.class);
        when(mkdirResult.getStatus()).thenReturn(GKFileActions.Status.SUCCESS);
        when(fileActions.makeDirectory(GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, licenseSubdir))).thenReturn(mkdirResult);

        when(licenseManager.generateID(serialContents)).thenReturn(idContents);
        when(licenseManager.activateOnline(idContents)).thenReturn(licenseContents);
    }

    @After
    public void tearDown() {
        tempSerialNumberFile.delete();
    }

    @Test(expected = IOException.class)
    public void throwsExceptionIfGetFileReturnsNonSuccessStatus() throws Exception {
        GKFileActions.GetFileResult getResult = mock(GKFileActions.GetFileResult.class);
        when(getResult.getStatus()).thenReturn(GKFileActions.Status.UNKNOWN_STATUS);
        when(fileActions.getFile(eq(serialFile), any(File.class))).thenReturn(getResult);

        subject.execute(serialFile);
    }

    @Test
    public void returnsLicenseWhenAllStepsSucceed() throws Exception {
        when(fileActions.getFile(eq(serialFile), any(File.class))).thenReturn(successfulGetFileResultForSerial);

        String license = subject.execute(serialFile);

        verify(licenseManager).generateID(serialContents);
        verify(licenseManager).activateOnline(idContents);
        assertThat(license, is(equalTo(licenseContents)));
    }

    @Test
    public void createsTheLicenseSubdirectoryIfItDoesntExist() throws Exception {
        when(fileActions.getFile(eq(serialFile), any(File.class))).thenReturn(successfulGetFileResultForSerial);

        subject.execute(serialFile);

        verify(fileActions).makeDirectory(GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, licenseSubdir));
    }

    @Test
    public void doesNotCreateTheLicenseSubdirectoryIfItExists() throws Exception {
        when(fileActions.getFile(eq(serialFile), any(File.class))).thenReturn(successfulGetFileResultForSerial);

        List<GKFile> files = new ArrayList<>();
        files.add(serialFile);
        files.add(new GKFile(licenseSubdir, GKFile.Type.DIRECTORY));

        GKFileActions.ListFilesResult listResult = mock(GKFileActions.ListFilesResult.class);
        when(listResult.getStatus()).thenReturn(GKFileActions.Status.SUCCESS);
        when(listResult.getFiles()).thenReturn(files);
        when(fileActions.listFiles(GKFileUtils.LICENSE_ROOT)).thenReturn(listResult);

        subject.execute(serialFile);

        verify(fileActions, never()).makeDirectory(GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, licenseSubdir));
    }

    @Test(expected = IOException.class)
    public void throwsExceptionIfMakingTheLicenseSubDirFails() throws Exception {
        when(fileActions.getFile(eq(serialFile), any(File.class))).thenReturn(successfulGetFileResultForSerial);

        GKFileActions.FileResult mkdirResult = mock(GKFileActions.FileResult.class);
        when(mkdirResult.getStatus()).thenReturn(GKFileActions.Status.UNKNOWN_STATUS);
        when(fileActions.makeDirectory(GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, licenseSubdir))).thenReturn(mkdirResult);

        subject.execute(serialFile);
    }

    @Test(expected = IOException.class)
    public void doesNotCreateTheLicenseIfWritingTheIdFileFails() throws Exception {
        when(fileActions.getFile(eq(serialFile), any(File.class))).thenReturn(successfulGetFileResultForSerial);

        String idContents = "test-id\n";
        when(licenseManager.generateID(serialContents)).thenReturn(idContents);

        String idCardPath = GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, licenseSubdir, "test.id");
        GKFileActions.PutFileResult failResult = mock(GKFileActions.PutFileResult.class);
        when(failResult.getStatus()).thenReturn(GKFileActions.Status.UNKNOWN_STATUS);
        when(fileActions.putFile(inputStreamMatching(idContents), contains(idCardPath))).thenReturn(failResult);

        subject.execute(serialFile);
    }

    @Test(expected = IOException.class)
    public void deactivatesTheLicenseIfWritingTheLicenseFileFails() throws Exception {
        when(fileActions.getFile(eq(serialFile), any(File.class))).thenReturn(successfulGetFileResultForSerial);

        String licenseCardPath = GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, licenseSubdir, "test.lic");
        GKFileActions.PutFileResult failResult = mock(GKFileActions.PutFileResult.class);
        when(failResult.getStatus()).thenReturn(GKFileActions.Status.UNKNOWN_STATUS);
        when(fileActions.putFile(inputStreamMatching(licenseContents), contains(licenseCardPath))).thenReturn(failResult);

        subject.execute(serialFile);
    }

    @Test
    public void deactivatesTheLicenseIfWritingTheLicenseFileThrowsAnException() throws Exception {
        when(fileActions.getFile(eq(serialFile), any(File.class))).thenReturn(successfulGetFileResultForSerial);

        String licenseCardPath = GKFileUtils.joinPath(GKFileUtils.LICENSE_ROOT, licenseSubdir, "test.lic");
        when(fileActions.putFile(inputStreamMatching(licenseContents), contains(licenseCardPath))).thenThrow(IOException.class);

        try {
            subject.execute(serialFile);
            fail("Exception expected but not thrown");
        } catch (IOException e) {
            verify(licenseManager).deactivateOnline(licenseContents);
            verify(fileActions, never()).deleteFile(any(GKFile.class));
        }
    }

    private InputStream inputStreamMatching(String expected) {
        return argThat(new InputStreamMatcher(expected));
    }

    public class InputStreamMatcher extends BaseMatcher<InputStream> {
        private final String mExpectedData;

        public InputStreamMatcher(String expectedData) {
            mExpectedData = expectedData;
        }

        @Override
        public boolean matches(Object argument) {
            InputStream inputStream = (InputStream) argument;
            Collection<String> strings = IOUtil.readLines(inputStream);
            String actualValue = GKStringUtils.join(strings.toArray(new String[strings.size()]), "\n") + "\n";
            try {
                inputStream.reset();
            } catch (IOException e) {
                fail("error resetting input stream");
            }
            return actualValue.equals(mExpectedData);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("\"" + mExpectedData + "\"");
        }
    }
}
