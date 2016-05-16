package co.blustor.gatekeepersdk.utils;

import org.hamcrest.core.IsEqual;
import org.junit.Test;

import co.blustor.gatekeepersdk.data.GKFile;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class GKFileUtilsTest {
    @Test
    public void joinPathCreatesAValidFilePathWithMultipleParts() {
        assertThat(GKFileUtils.joinPath("test", "ing", "this"), is(equalTo("test/ing/this")));
    }

    @Test
    public void joinPathReturnsEmptyStringWhenPassedNothing() {
        assertThat(GKFileUtils.joinPath(), is(emptyString()));
    }

    @Test
    public void joinPathRemovesExtraneousSlashes() {
        assertThat(GKFileUtils.joinPath("test////", "/////this"), is("test/this"));
    }

    @Test
    public void addExtensionBuildsAFilePathWithExtension() {
        assertThat(GKFileUtils.addExtension("test", "txt"), is(equalTo("test.txt")));
    }

    @Test
    public void addExtensionIgnoresTheExtensionIfNullOrEmpty() {
        assertThat(GKFileUtils.addExtension("test", null), is(equalTo("test")));
        assertThat(GKFileUtils.addExtension("test", ""), is(equalTo("test")));
    }

    @Test
    public void addExtensionReturnsEmptyStringIfPathIsNullOrEmpty() {
        assertThat(GKFileUtils.addExtension(null, "txt"), is(equalTo("")));
        assertThat(GKFileUtils.addExtension("", "txt"), is(equalTo("")));
    }

    @Test
    public void parseFileParsesFileListEntries() {
        String fileData = "-rw-rw-rw-   1  root root 449060 Nov 26  2015 test-file.jpg\r\n";

        GKFile gkFile = GKFileUtils.parseFile(fileData);

        assertThat(gkFile.getExtension(), is(IsEqual.equalTo("jpg")));
        assertThat(gkFile.getFilenameBase(), is(IsEqual.equalTo("test-file")));
        assertThat(gkFile.getType(), is(IsEqual.equalTo(GKFile.Type.FILE)));
        assertThat(gkFile.getName(), is(IsEqual.equalTo("test-file.jpg")));
        assertThat(gkFile.getFileSize(), is(IsEqual.equalTo(449060)));
    }

    @Test
    public void parseFileReturnsNullIfFileCannotBeParsed() {
        String fileData = "-rw-rw-rw-\r\n";

        assertThat(GKFileUtils.parseFile(fileData), is(nullValue()));
    }
}
