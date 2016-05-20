package co.blustor.gatekeepersdk.services;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeepersdk.data.GKFile;
import co.blustor.gatekeepersdk.devices.GKCard;
import co.blustor.gatekeepersdk.utils.TestFileUtil;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GKFileActionsTest {

    private GKCard card;
    private GKFileActions fileActions;

    @Before
    public void setUp() throws Exception {
        card = mock(GKCard.class);
        fileActions = new GKFileActions(card);
    }

    @Test
    public void listFilesReturnsEmptyFileListIfThereIsNoDataFile() throws IOException {
        String cardPath = "/test";
        byte[] commandData = "530".getBytes();
        GKCard.Response response = new GKCard.Response(commandData, null);
        when(card.list(cardPath)).thenReturn(response);

        GKFileActions.ListFilesResult result = fileActions.listFiles(cardPath);

        assertThat(result.getFiles(), is(empty()));
    }

    @Test
    public void listFilesReturnsEmptyFileListIfDataFileIsEmpty() throws IOException {
        String cardPath = "/test";
        byte[] commandData = "226".getBytes();
        File dataFile = TestFileUtil.buildTempFile();
        GKCard.Response response = new GKCard.Response(commandData, dataFile);
        when(card.list(cardPath)).thenReturn(response);

        GKFileActions.ListFilesResult result = fileActions.listFiles(cardPath);

        assertThat(result.getFiles(), is(empty()));
    }

    @Test
    public void listFilesListOfParsedGKFileObjects() throws IOException {
        String cardPath = "/test";
        byte[] commandData = "226".getBytes();
        File dataFile = TestFileUtil.buildTempFile();
        String data = "-rw-rw-rw- 1 root root 449060 Nov 26 2015 test-file.jpg\r\n" +
                "drw-rw-rw- 1 root root 123 Nov 26 2015 test-dir\r\n";
        TestFileUtil.writeToFile(dataFile, data);
        GKCard.Response response = new GKCard.Response(commandData, dataFile);
        when(card.list(cardPath)).thenReturn(response);

        GKFileActions.ListFilesResult result = fileActions.listFiles(cardPath);
        assertThat(result.getFiles().size(), is(2));

        GKFile gkFile = result.getFiles().get(0);
        assertThat(gkFile.getCardPath(), is(equalTo("/test/test-file.jpg")));
        assertThat(gkFile.getExtension(), is(equalTo("jpg")));
        assertThat(gkFile.getFilenameBase(), is(equalTo("test-file")));
        assertThat(gkFile.getType(), is(equalTo(GKFile.Type.FILE)));
        assertThat(gkFile.getName(), is(equalTo("test-file.jpg")));
        assertThat(gkFile.getFileSize(), is(equalTo(449060)));

        GKFile dir = result.getFiles().get(1);
        assertThat(dir.getCardPath(), is(equalTo("/test/test-dir")));
        assertThat(dir.getExtension(), is(nullValue()));
        assertThat(dir.getFilenameBase(), is(nullValue()));
        assertThat(dir.getType(), is(equalTo(GKFile.Type.DIRECTORY)));
        assertThat(dir.getName(), is(equalTo("test-dir")));
        assertThat(dir.getFileSize(), is(equalTo(123)));
    }

    @Test
    public void getFileWritesResponseDataToLocalFile() throws IOException {
        GKFile gkFile = new GKFile("test", GKFile.Type.FILE);
        File localFile = mock(File.class);
        GKCard.Response response = new GKCard.Response(226, "cool", localFile);
        when(card.get(gkFile.getCardPath(), localFile)).thenReturn(response);

        GKFileActions.GetFileResult result = fileActions.getFile(gkFile, localFile);

        assertThat(result.getFile(), is(localFile));
        assertThat(result.getStatus(), is(GKFileActions.Status.SUCCESS));
    }

    @Test
    public void putFileReturnsGKFileRepresentingTheCreatedFile() throws IOException {
        String cardPath = "/card/path/test.txt";
        InputStream inputStream = mock(InputStream.class);

        GKCard.Response putResponse = new GKCard.Response(226, "test");
        when(card.put(cardPath, inputStream)).thenReturn(putResponse);

        GKCard.Response finalizeResponse = new GKCard.Response(213, "1:" + cardPath);
        when(card.finalize(cardPath)).thenReturn(finalizeResponse);

        GKFileActions.PutFileResult result = fileActions.putFile(inputStream, cardPath);

        assertThat(result.getFile().getCardPath(), is(equalTo(cardPath)));
    }

    @Test
    public void renameFileRenamesTheFileRelativeToItsCurrentCardPath() throws IOException {
        String fileLocation = "/test/ing/";
        String originalName = "this.gif";
        GKFile file = new GKFile(originalName, GKFile.Type.FILE);
        file.setCardPath(fileLocation + originalName);

        when(card.rename(anyString(), anyString())).thenReturn(new GKCard.Response(250, ""));

        String newName = "that.gif";
        GKFileActions.FileResult result = fileActions.renameFile(file, newName);

        assertThat(result.getStatus(), is(equalTo(GKFileActions.Status.SUCCESS)));
        verify(card).rename(fileLocation + originalName, fileLocation + newName);
    }

    @Test
    public void renameFileReturnsErrorIfThereIsAnError() throws IOException {
        String originalName = "this.gif";
        GKFile file = new GKFile(originalName, GKFile.Type.FILE);
        file.setCardPath("/test/ing/" + originalName);

        when(card.rename(anyString(), anyString())).thenReturn(new GKCard.Response(550, ""));

        GKFileActions.FileResult result = fileActions.renameFile(file, "that.gif");

        assertThat(result.getStatus(), is(equalTo(GKFileActions.Status.NOT_FOUND)));
    }
}
