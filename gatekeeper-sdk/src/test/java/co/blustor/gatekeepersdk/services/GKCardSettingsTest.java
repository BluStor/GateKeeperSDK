package co.blustor.gatekeepersdk.services;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import co.blustor.gatekeepersdk.devices.GKBluetoothCard;
import co.blustor.gatekeepersdk.devices.GKCard;
import co.blustor.gatekeepersdk.utils.TestFileUtil;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
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
}
