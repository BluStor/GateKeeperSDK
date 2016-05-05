package co.blustor.gatekeepersdk.biometrics.licensing;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import co.blustor.gatekeepersdk.data.GKFile;
import co.blustor.gatekeepersdk.services.GKFileActions;
import co.blustor.gatekeepersdk.utils.TestFileUtil;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FetchExistingLicenseTest {
    private static final String licenseContents = "test\nlicense\n";
    private FetchExistingLicense subject;
    private GKFileActions fileActions;
    private File tempLicenseFile;
    private GKFile licenseFile;

    @Before
    public void setUp() throws IOException {
        fileActions = mock(GKFileActions.class);
        subject = new FetchExistingLicense(fileActions);

        tempLicenseFile = TestFileUtil.buildTempFile();
        FileWriter licenseWriter = new FileWriter(tempLicenseFile);
        licenseWriter.write(licenseContents);
        licenseWriter.close();

        licenseFile = new GKFile("test.lic", GKFile.Type.FILE);
    }

    @Test(expected = IOException.class)
    public void throwsExceptionIfGetFileReturnsNonSuccessStatus() throws Exception {
        GKFileActions.GetFileResult getResult = mock(GKFileActions.GetFileResult.class);
        when(getResult.getStatus()).thenReturn(GKFileActions.Status.UNKNOWN_STATUS);
        when(fileActions.getFile(eq(licenseFile), any(File.class))).thenReturn(getResult);

        subject.execute(licenseFile);
    }

    @Test
    public void returnsLicenseTextWhenReadingFileSucceeds() throws Exception {
        GKFileActions.GetFileResult getFileResult = mock(GKFileActions.GetFileResult.class);
        when(getFileResult.getStatus()).thenReturn(GKFileActions.Status.SUCCESS);
        when(getFileResult.getFile()).thenReturn(tempLicenseFile);

        when(fileActions.getFile(eq(licenseFile), any(File.class))).thenReturn(getFileResult);

        String license = subject.execute(licenseFile);

        assertThat(license, is(equalTo(licenseContents)));
    }
}
