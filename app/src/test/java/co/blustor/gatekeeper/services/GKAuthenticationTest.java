package co.blustor.gatekeeper.services;

import org.junit.Test;

import co.blustor.gatekeeper.devices.GKBluetoothCard;
import co.blustor.gatekeeper.devices.GKCard;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class GKAuthenticationTest {

    @Test
    public void revokePinRetunsSuccessWhenAuthenticatedTest() throws Exception {
        GKCard fakeCard = mock(GKBluetoothCard.class);

        when(fakeCard.delete(GKAuthentication.AUTH_ENROLL_PIN))
                .thenReturn(new GKCard.Response(250, "Pin Unset"));

        GKAuthentication.AuthResult auth = new GKAuthentication(fakeCard).revokePin();
        assertThat(auth.getStatus(), is(equalTo(GKAuthentication.Status.SUCCESS)));
    }

    @Test
    public void revokePinReturnsUnauthenticatedTest() throws Exception {
        GKCard fakeCard = mock(GKBluetoothCard.class);

        when(fakeCard.delete(GKAuthentication.AUTH_ENROLL_PIN))
                .thenReturn(new GKCard.Response(530, "Unauthorized"));

        GKAuthentication.AuthResult auth = new GKAuthentication(fakeCard).revokePin();
        assertThat(auth.getStatus(), is(equalTo(GKAuthentication.Status.UNAUTHORIZED)));
    }
}
