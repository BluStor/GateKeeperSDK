package co.blustor.gatekeepersdk.services;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeepersdk.data.GKFile;
import co.blustor.gatekeepersdk.devices.GKCard;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
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
    public void listFilesReturnsEmptyFileListIfThereIsNoData() throws IOException {
        String cardPath = "/test";
        byte[] commandData = "530".getBytes();
        GKCard.Response response = new GKCard.Response(commandData, null);
        when(card.list(cardPath)).thenReturn(response);

        GKFileActions.ListFilesResult result = fileActions.listFiles(cardPath);

        assertThat(result.getFiles(), is(empty()));
    }

    @Test
    public void listFilesListOfParsedGKFileObjects() throws IOException {
        String cardPath = "/test";
        byte[] commandData = "226".getBytes();
        byte[] responseData = "-rw-rw-rw- 1 root root 449060 Nov 26 2015 test-file.jpg\r\n".getBytes();
        GKCard.Response response = new GKCard.Response(commandData, responseData);
        when(card.list(cardPath)).thenReturn(response);

        GKFileActions.ListFilesResult result = fileActions.listFiles(cardPath);
        GKFile gkFile = result.getFiles().get(0);

        assertThat(gkFile.getCardPath(), is(equalTo("/test/test-file.jpg")));
        assertThat(gkFile.getExtension(), is(equalTo("jpg")));
        assertThat(gkFile.getFilenameBase(), is(equalTo("test-file")));
        assertThat(gkFile.getType(), is(equalTo(GKFile.Type.FILE)));
        assertThat(gkFile.getName(), is(equalTo("test-file.jpg")));
        assertThat(gkFile.getFileSize(), is(equalTo(449060)));
    }

    @Test
    public void putFileReturnsGKFileRepresentingTheCreatedFile() throws IOException {
        GKCard card = mock(GKCard.class);
        GKFileActions fileActions = new GKFileActions(card);
        String cardPath = "/card/path/test.txt";
        InputStream inputStream = mock(InputStream.class);

        GKCard.Response putResponse = new GKCard.Response(226, "test");
        when(card.put(cardPath, inputStream)).thenReturn(putResponse);

        GKCard.Response finalizeResponse = new GKCard.Response(213, "1:" + cardPath);
        when(card.finalize(cardPath)).thenReturn(finalizeResponse);

        GKFileActions.PutFileResult result = fileActions.putFile(inputStream, cardPath);

        assertThat(result.getFile().getCardPath(), is(equalTo(cardPath)));
    }
}
