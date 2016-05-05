package co.blustor.gatekeepersdk.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestFileUtil {
    public static File buildTempFile() throws IOException {
        File temp = File.createTempFile("test", "ing");
        temp.deleteOnExit();
        return temp;
    }

    public static void writeToFile(File file, String data) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
        fileOutputStream.close();
    }
}
