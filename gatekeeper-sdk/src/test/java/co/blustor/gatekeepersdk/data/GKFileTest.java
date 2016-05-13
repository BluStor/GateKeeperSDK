package co.blustor.gatekeepersdk.data;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class GKFileTest {

    @Test
    public void getExtensionReturnsTheCorrectExtensionFromFileNameIfTypeIsFILE() {
        List<String[]> data = Arrays.asList(new String[][]{
                {"testing.zip", "zip"},
                {"testing.docs.tar", "tar"},
                {"testing_this.aar", "aar"},
                {"testing-this_thing right here3.0n4.test.txt", "txt"},
                {"testing", null}
        });

        for (String[] values : data) {
            GKFile file = new GKFile(values[0], GKFile.Type.FILE);
            assertThat(file.getExtension(), is(equalTo(values[1])));
        }
    }

    @Test
    public void getExtensionReturnsNullIfTypeIsDIRECTORY() {
        GKFile file = new GKFile("directory.zip", GKFile.Type.DIRECTORY);
        assertThat(file.getExtension(), is(nullValue()));
    }

    @Test
    public void getFilenameBaseReturnsTheFilenameWithoutExtensionIfTypeIsFILE() {
        List<String[]> data = Arrays.asList(new String[][]{
                {"testing.zip", "testing"},
                {"testing.docs.tar", "testing.docs"},
                {"testing_this.aar", "testing_this"},
                {"testing-this_thing right here3.0n4.test.txt", "testing-this_thing right here3.0n4.test"},
                {"no-extension", "no-extension"}
        });

        for (String[] values : data) {
            GKFile file = new GKFile(values[0], GKFile.Type.FILE);
            assertThat(file.getFilenameBase(), is(equalTo(values[1])));
        }
    }

    @Test
    public void getFilenameBaseReturnsNullIfTypeIsDIRECTORY() {
        GKFile file = new GKFile("directory.zip", GKFile.Type.DIRECTORY);
        assertThat(file.getFilenameBase(), is(nullValue()));
    }
}
