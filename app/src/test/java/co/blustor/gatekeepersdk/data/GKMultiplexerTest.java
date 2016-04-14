package co.blustor.gatekeepersdk.data;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import co.blustor.gatekeepersdk.devices.CommPort;
import co.blustor.gatekeepersdk.devices.IOCommPort;

import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class GKMultiplexerTest {

    private GKMultiplexer multiplexer;
    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        outputStream = new ByteArrayOutputStream();
        IOCommPort commPort = new IOCommPort(inputStream, outputStream);
        multiplexer = new GKMultiplexer(commPort);
    }

    @Test
    public void writeToCommandChannelConstructsPacketAndWritesItToCommandChannel() throws IOException {
        byte[] input = {'x', 'y', 'z', 'j'};

        multiplexer.writeToCommandChannel(input);

        byte[] expectedPacket = {GKMultiplexer.COMMAND_CHANNEL, 0, 9, 'x', 'y', 'z', 'j', 0, 0};
        assertThat(outputStream.toByteArray(), is(Matchers.equalTo(expectedPacket)));
    }

    @Test
    public void writeToDataChannelConstructsPacketAndWritesItToDataChannel() throws IOException {
        byte[] input = {'x', 'y', 'z', 'j'};

        multiplexer.writeToDataChannel(input);

        byte[] expectedPacket = {GKMultiplexer.DATA_CHANNEL, 0, 9, 'x', 'y', 'z', 'j', 0, 0};
        assertThat(outputStream.toByteArray(), is(Matchers.equalTo(expectedPacket)));
    }

    @Test
    public void writeToDataChannelWithInputStreamBuffersPackets() throws Exception {
        byte[] input = new byte[GKMultiplexer.MAXIMUM_PAYLOAD_SIZE + 1];
        Arrays.fill(input, (byte) 'x');

        multiplexer.writeToDataChannel(new ByteArrayInputStream(input));

        // should write in 2 packets of up to 512 bytes each, so 5 extra bytes per packet
        assertThat(outputStream.toByteArray().length, is(equalTo(523)));
        byte[] firstPacketStart = {GKMultiplexer.DATA_CHANNEL, 2, 5, 'x'};
        assertThat(Arrays.copyOfRange(outputStream.toByteArray(), 0, 4), is(Matchers.equalTo(firstPacketStart)));
        byte[] secondPacket = {GKMultiplexer.DATA_CHANNEL, 0, 6, 'x', 0, 0};
        assertThat(Arrays.copyOfRange(outputStream.toByteArray(), 517, 523), is(Matchers.equalTo(secondPacket)));
    }
}
