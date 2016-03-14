package co.blustor.gatekeepersdk.utils;

import org.junit.Test;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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
}
