package co.blustor.gatekeepersdk.services;

import org.junit.Test;

import java.io.IOException;

import co.blustor.gatekeepersdk.devices.GKBluetoothCard;
import co.blustor.gatekeepersdk.devices.GKCard;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GKCardSettingsTest {
    @Test
    public void getFirmwareInformationRetrievesTheVersion() throws IOException {
        GKCard card = mock(GKBluetoothCard.class);
        GKCard.Response response = new GKCard.Response(226, "");
        response.setData("BOOT:  2.0\r\nFIRM:    0.4.0\r\nTransfer complete.".getBytes());
        when(card.get("/device/firmware")).thenReturn(response);

        GKCardSettings settings = new GKCardSettings(card);
        GKCardSettings.FirmwareInformationResult result = settings.getFirmwareInformation();

        assertThat(result.getStatus(), equalTo(GKCardSettings.Status.SUCCESS));
        assertThat(result.getBootVersion(), equalTo("2.0"));
        assertThat(result.getFirmwareVersion(), equalTo("0.4.0"));
    }

    @Test
    public void getFirmwareInformationReturnsErrorResultOnFailure() throws IOException {
        GKCard card = mock(GKBluetoothCard.class);
        GKCard.Response response = new GKCard.Response(530, "");
        when(card.get("/device/firmware")).thenReturn(response);

        GKCardSettings settings = new GKCardSettings(card);
        GKCardSettings.FirmwareInformationResult result = settings.getFirmwareInformation();

        assertThat(result.getStatus(), is(GKCardSettings.Status.UNAUTHORIZED));
        assertThat(result.getBootVersion(), is(nullValue()));
        assertThat(result.getFirmwareVersion(), is(nullValue()));
    }
}
