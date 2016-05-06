package co.blustor.gatekeepersdk.data;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import co.blustor.gatekeepersdk.utils.GKFileUtils;
import co.blustor.gatekeepersdk.utils.TestFileUtil;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class GKMultiplexerTest {
    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() throws Exception {
        outputStream = new ByteArrayOutputStream();
    }

    @Test
    public void writeToCommandChannelConstructsPacketAndWritesItToCommandChannel() throws IOException {
        byte[] input = {'x', 'y', 'z', 'j'};
        GKMultiplexer multiplexer = buildMultiplexer(new byte[0]);

        multiplexer.writeToCommandChannel(input);

        byte[] expectedPacket = {GKMultiplexer.COMMAND_CHANNEL, 0, 9, 'x', 'y', 'z', 'j', 0, 0};
        assertThat(outputStream.toByteArray(), is(equalTo(expectedPacket)));
    }

    @Test
    public void writeToDataChannelConstructsPacketAndWritesItToDataChannel() throws IOException {
        byte[] input = {'x', 'y', 'z', 'j'};
        GKMultiplexer multiplexer = buildMultiplexer(new byte[0]);

        multiplexer.writeToDataChannel(input);

        byte[] expectedPacket = {GKMultiplexer.DATA_CHANNEL, 0, 9, 'x', 'y', 'z', 'j', 0, 0};
        assertThat(outputStream.toByteArray(), is(equalTo(expectedPacket)));
    }

    @Test
    public void writeToDataChannelWithInputStreamBuffersPackets() throws Exception {
        byte[] input = new byte[GKMultiplexer.MAXIMUM_PAYLOAD_SIZE + 1];
        Arrays.fill(input, (byte) 'x');
        GKMultiplexer multiplexer = buildMultiplexer(new byte[0]);

        multiplexer.writeToDataChannel(new ByteArrayInputStream(input));

        // should write in 2 packets of up to MAXIMUM_PAYLOAD_SIZE bytes each, so 5 extra bytes per packet
        assertThat(outputStream.toByteArray().length, is(equalTo(GKMultiplexer.MAXIMUM_PAYLOAD_SIZE + 11)));
        byte[] firstPacketStart = {GKMultiplexer.DATA_CHANNEL, GKMultiplexer.MAXIMUM_PAYLOAD_SIZE >> 8, 5, 'x'};
        assertThat(Arrays.copyOfRange(outputStream.toByteArray(), 0, 4), is(equalTo(firstPacketStart)));
        byte[] secondPacket = {GKMultiplexer.DATA_CHANNEL, 0, 6, 'x', 0, 0};
        assertThat(Arrays.copyOfRange(outputStream.toByteArray(), GKMultiplexer.MAXIMUM_PAYLOAD_SIZE + 5, GKMultiplexer.MAXIMUM_PAYLOAD_SIZE + 11), is(equalTo(secondPacket)));
    }

    @Test
    public void readCommandChannelLineReadsUntilCRLF() throws IOException {
        String commandLine1 = "reading this";
        String commandLine2 = "this comes next";
        byte[] commandResponse1 = DataPacket.Builder.toPacketBytes((commandLine1 + "\r\n    not read").getBytes(), GKMultiplexer.COMMAND_CHANNEL);
        byte[] commandResponse2 = DataPacket.Builder.toPacketBytes((commandLine2 + "\r\n").getBytes(), GKMultiplexer.COMMAND_CHANNEL);
        byte[] response = concat(commandResponse1, commandResponse2);
        GKMultiplexer multiplexer = buildMultiplexer(response);

        assertThat(multiplexer.readCommandChannelLine(), is(equalTo("reading this".getBytes())));
        assertThat(multiplexer.readCommandChannelLine(), is(equalTo("this comes next".getBytes())));
    }

    @Test
    public void readDataChannelToFileCopiesDataChannelDataToFile() throws IOException {
        File dataFile = TestFileUtil.buildTempFile();
        String dataLine = "this will be in a file";
        String commandLine = "226 Transfer Complete.\r\n";
        byte[] dataResponse = DataPacket.Builder.toPacketBytes(dataLine.getBytes(), GKMultiplexer.DATA_CHANNEL);
        byte[] commandResponse = DataPacket.Builder.toPacketBytes(commandLine.getBytes(), GKMultiplexer.COMMAND_CHANNEL);

        GKMultiplexer multiplexer = buildMultiplexer(concat(dataResponse, commandResponse));
        multiplexer.readDataChannelToFile(dataFile);

        assertThat(readFile(dataFile), is(equalTo(dataLine)));
    }

    @Test
    public void readDataChannelToFileCopiesMultipleDataChannelPacketsToFileUntilCommandChannelPacketComesUp() throws IOException {
        File dataFile = TestFileUtil.buildTempFile();
        String dataLine1 = "This will be in a file";
        String dataLine2 = ". So will this.";
        String commandLine = "226 Transfer Complete.\r\n";
        byte[] dataResponse1 = DataPacket.Builder.toPacketBytes(dataLine1.getBytes(), GKMultiplexer.DATA_CHANNEL);
        byte[] dataResponse2 = DataPacket.Builder.toPacketBytes(dataLine2.getBytes(), GKMultiplexer.DATA_CHANNEL);
        byte[] commandResponse = DataPacket.Builder.toPacketBytes(commandLine.getBytes(), GKMultiplexer.COMMAND_CHANNEL);
        byte[] data = concat(dataResponse1, concat(dataResponse2, commandResponse));

        GKMultiplexer multiplexer = buildMultiplexer(data);
        multiplexer.readDataChannelToFile(dataFile);

        assertThat(readFile(dataFile), is(equalTo(dataLine1 + dataLine2)));
    }

    @Test
    public void readDataChannelReturnsTheResponseFromTheCommandChannel() throws IOException {
        File dataFile = TestFileUtil.buildTempFile();
        String dataLine = "x";
        String commandLine = "226 Transfer Complete.";
        byte[] dataResponse = DataPacket.Builder.toPacketBytes(dataLine.getBytes(), GKMultiplexer.DATA_CHANNEL);
        byte[] commandResponse = DataPacket.Builder.toPacketBytes((commandLine + "\r\n").getBytes(), GKMultiplexer.COMMAND_CHANNEL);

        GKMultiplexer multiplexer = buildMultiplexer(concat(dataResponse, commandResponse));
        byte[] commandData = multiplexer.readDataChannelToFile(dataFile);

        assertThat(commandData, is(equalTo(commandLine.getBytes())));
    }

    @Test
    public void readDataChannelReadsPacketsUntilACommandPacketHasCRLF() throws IOException {
        File dataFile = TestFileUtil.buildTempFile();
        String dataLine = "x";
        String commandLine1 = "226 Transfer Complete.";
        String commandLine2 = "for real. done.";
        byte[] dataResponse = DataPacket.Builder.toPacketBytes(dataLine.getBytes(), GKMultiplexer.DATA_CHANNEL);
        byte[] commandResponse1 = DataPacket.Builder.toPacketBytes(commandLine1.getBytes(), GKMultiplexer.COMMAND_CHANNEL);
        byte[] commandResponse2 = DataPacket.Builder.toPacketBytes((commandLine2 + "\r\n").getBytes(), GKMultiplexer.COMMAND_CHANNEL);
        byte[] response = concat(dataResponse, concat(commandResponse1, commandResponse2));

        GKMultiplexer multiplexer = buildMultiplexer(response);
        byte[] commandData = multiplexer.readDataChannelToFile(dataFile);

        assertThat(commandData, is(equalTo((commandLine1 + commandLine2).getBytes())));
    }

    private GKMultiplexer buildMultiplexer(byte[] responseData) {
        return new GKMultiplexer(new ByteArrayInputStream(responseData), outputStream);
    }

    private byte[] concat(byte[] x, byte[] y) {
        byte[] result = new byte[x.length + y.length];
        System.arraycopy(x, 0, result, 0, x.length);
        System.arraycopy(y, 0, result, x.length, y.length);

        return result;
    }

    private String readFile(File file) throws IOException {
        String contentsPlusNewline = GKFileUtils.readFile(file);
        // -1 because GKFileUtils.readFile adds a newline at the end
        return contentsPlusNewline.substring(0, contentsPlusNewline.length() - 1);
    }
}
