package co.blustor.gatekeepersdk.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeepersdk.data.GKCardConfiguration;
import co.blustor.gatekeepersdk.devices.GKBluetoothCard;
import co.blustor.gatekeepersdk.devices.GKCard;
import co.blustor.gatekeepersdk.utils.TestFileUtil;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GKCardSettingsTest {

    private GKCard card;
    private GKCardSettings settings;

    @Before
    public void setUp() throws Exception {
        card = mock(GKBluetoothCard.class);
        settings = new GKCardSettings(card);
    }

    @Test
    public void getFirmwareInformationRetrievesTheVersion() throws IOException {
        File dataFile = TestFileUtil.buildTempFile();
        TestFileUtil.writeToFile(dataFile, "BOOT:  2.0\r\nFIRM:    0.4.0\r\nTransfer complete.");
        GKCard.Response response = new GKCard.Response(226, "", dataFile);
        when(card.get("/device/firmware")).thenReturn(response);

        GKCardSettings.FirmwareInformationResult result = settings.getFirmwareInformation();

        assertThat(result.getStatus(), equalTo(GKCardSettings.Status.SUCCESS));
        assertThat(result.getBootVersion(), equalTo("2.0"));
        assertThat(result.getFirmwareVersion(), equalTo("0.4.0"));
    }

    @Test
    public void getFirmwareInformationReturnsErrorResultWhenVersionsCannotBeDetermined() throws IOException {
        File dataFile = TestFileUtil.buildTempFile();
        GKCard.Response response = new GKCard.Response(226, "", dataFile);
        when(card.get("/device/firmware")).thenReturn(response);

        GKCardSettings.FirmwareInformationResult result = settings.getFirmwareInformation();

        assertThat(result.getStatus(), equalTo(GKCardSettings.Status.UNKNOWN_STATUS));
        assertThat(result.getBootVersion(), is(nullValue()));
        assertThat(result.getFirmwareVersion(), is(nullValue()));
    }

    @Test
    public void getFirmwareInformationReturnsErrorResultOnFailure() throws IOException {
        GKCard.Response response = new GKCard.Response(530, "");
        when(card.get("/device/firmware")).thenReturn(response);

        GKCardSettings.FirmwareInformationResult result = settings.getFirmwareInformation();

        assertThat(result.getStatus(), is(GKCardSettings.Status.UNAUTHORIZED));
        assertThat(result.getBootVersion(), is(nullValue()));
        assertThat(result.getFirmwareVersion(), is(nullValue()));
    }

    @Test
    public void getCardSettingsReturnsNotFoundResponseWhenResultIs550() throws IOException {
        GKCard.Response response = new GKCard.Response(550, "");
        when(card.get("/device/settings")).thenReturn(response);

        GKCardSettings.CardSettingsResult result = settings.getCardSettings();

        assertThat(result.getStatus(), is(GKCardSettings.Status.NOT_FOUND));
    }

    @Test
    public void getCardSettingsReturnsTheAuthenticationTimeout() throws IOException {
        File dataFile = TestFileUtil.buildTempFile();
        TestFileUtil.writeToFile(dataFile, "{\"authentication_to\": 300}");
        GKCard.Response response = new GKCard.Response(226, "", dataFile);
        when(card.get("/device/settings")).thenReturn(response);

        GKCardSettings.CardSettingsResult result = settings.getCardSettings();

        assertThat(result.getStatus(), is(GKCardSettings.Status.SUCCESS));
        assertThat(result.getCardConfig().getAuthTimeout(), is(300));
    }

    @Test
    public void updateCardSettingsSendsTheConfigJsonToTheCard() throws IOException {
        final String json = "{}";
        GKCardConfiguration config = mock(GKCardConfiguration.class);
        when(config.getConfigJson()).thenReturn(json);
        when(card.put(eq("/device/settings"), any(InputStream.class))).thenAnswer(new Answer<GKCard.Response>() {
            @Override
            public GKCard.Response answer(InvocationOnMock invocation) throws Throwable {
                InputStream inputStream = (InputStream) invocation.getArguments()[1];
                byte[] fileContents = new byte[json.length()];
                inputStream.read(fileContents);
                assertThat(new String(fileContents), is(equalTo(json)));
                return new GKCard.Response(226, "");
            }
        });
        when(card.finalize("/device/settings")).thenReturn(new GKCard.Response(213, ""));

        GKCardSettings.CardResult result = settings.updateCardSettings(config);

        assertThat(result.getStatus(), is(GKCardSettings.Status.SUCCESS));
        verify(card).put(eq("/device/settings"), any(InputStream.class));
        verify(card).finalize("/device/settings");
    }

    @Test
    public void updateCardSettingsReturnsErrorResponseIfPutFails() throws IOException {
        GKCardConfiguration config = mock(GKCardConfiguration.class);
        when(config.getConfigJson()).thenReturn("");
        when(card.put(eq("/device/settings"), any(InputStream.class))).thenReturn(new GKCard.Response(500, ""));

        GKCardSettings.CardResult result = settings.updateCardSettings(config);

        assertThat(result.getStatus(), is(GKCardSettings.Status.UNKNOWN_STATUS));
        verify(card).put(eq("/device/settings"), any(InputStream.class));
        verify(card, never()).finalize("/device/settings");
    }
}
