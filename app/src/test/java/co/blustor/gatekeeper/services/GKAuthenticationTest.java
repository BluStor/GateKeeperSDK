package co.blustor.gatekeeper.services;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.blustor.gatekeeper.devices.GKCard;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class GKAuthenticationTest {
    private GKCard fakeCard = mock(GKCard.class);

    @Test
    public void revokePinRetunsSuccessWhenAuthenticatedTest() throws Exception {
        when(fakeCard.delete(GKAuthentication.AUTH_ENROLL_PIN))
                .thenReturn(new GKCard.Response(250, "Pin Unset"));

        GKAuthentication.AuthResult auth = new GKAuthentication(fakeCard).revokePin();

        assertThat(auth.getStatus(), is(equalTo(GKAuthentication.Status.SUCCESS)));
    }

    @Test
    public void revokePinReturnsUnauthenticatedTest() throws Exception {
        when(fakeCard.delete(GKAuthentication.AUTH_ENROLL_PIN))
                .thenReturn(new GKCard.Response(530, "Unauthorized"));

        GKAuthentication.AuthResult auth = new GKAuthentication(fakeCard).revokePin();

        assertThat(auth.getStatus(), is(equalTo(GKAuthentication.Status.UNAUTHORIZED)));
    }

    @Test
    public void enrollWithPinReturnsSuccessForSuccessfulPut() throws Exception {
        when(fakeCard.put(eq(GKAuthentication.AUTH_ENROLL_PIN), any(InputStream.class)))
                .thenReturn(new GKCard.Response(226, "success"));
        when(fakeCard.finalize(GKAuthentication.AUTH_ENROLL_PIN))
                .thenReturn(new GKCard.Response(226, "success"));

        GKAuthentication.AuthResult result = new GKAuthentication(fakeCard).enrollWithPin("1234");

        verify(fakeCard).finalize(GKAuthentication.AUTH_ENROLL_PIN);
        assertThat(result.getStatus(), is(equalTo(GKAuthentication.Status.SUCCESS)));
    }

    @Test
    public void enrollWithPinReturnsFailureWhenPutResponseStatusIsNot226() throws Exception {
        when(fakeCard.put(eq(GKAuthentication.AUTH_ENROLL_PIN), any(InputStream.class)))
                .thenReturn(new GKCard.Response(426, "success"));

        GKAuthentication.AuthResult result = new GKAuthentication(fakeCard).enrollWithPin("1234");

        assertThat(result.getStatus(), is(equalTo(GKAuthentication.Status.CANCELED)));
    }

    @Test
    public void signInlWithPinReturnsSuccessForSuccessfulPut() throws Exception {
        when(fakeCard.put(eq(GKAuthentication.SIGN_IN_PIN_PATH), any(InputStream.class)))
                .thenReturn(new GKCard.Response(226, "success"));
        when(fakeCard.finalize(GKAuthentication.SIGN_IN_PIN_PATH))
                .thenReturn(new GKCard.Response(226, "success"));

        GKAuthentication.AuthResult result = new GKAuthentication(fakeCard).signInWithPin("1234");

        verify(fakeCard).finalize(GKAuthentication.SIGN_IN_PIN_PATH);
        assertThat(result.getStatus(), is(equalTo(GKAuthentication.Status.SUCCESS)));
    }

    @Test
    public void returnsUnknownTemplateWhenNotAuthenticated() throws IOException {
        when(fakeCard.list(GKAuthentication.LIST_FACE_PATH))
                .thenReturn(new GKCard.Response(530, "Unauthorized"));
        GKAuthentication.ListTemplatesResult listTemplatesResult = new GKAuthentication(fakeCard).listTemplates();
        assertThat(listTemplatesResult.getTemplates(), hasItem(equalTo(GKAuthentication.ListTemplatesResult.UNKNOWN_TEMPLATE)));
    }

    @Test
    public void listTemplatesResultReturnsAllTemplates() throws IOException {
        GKCard.Response fakeResponse = mock(GKCard.Response.class);
        String data = "-rw-rw-rw-   1 root  root       4026 Dec 16  2015 1\r\n";
        when(fakeResponse.getData()).thenReturn(data.getBytes(StandardCharsets.UTF_8));
        when(fakeCard.list(GKAuthentication.LIST_FACE_PATH)).thenReturn(fakeResponse);
        GKAuthentication.ListTemplatesResult listTemplatesResult = new GKAuthentication(fakeCard).listTemplates();
        assertThat(listTemplatesResult.getTemplates(), hasItem(equalTo("1")));
    }

    @Test
    public void returnsEmptyListWhenGetDataIsNull() throws IOException {
        GKCard.Response fakeResponse = mock(GKCard.Response.class);
        when(fakeResponse.getData()).thenReturn(null);
        when(fakeCard.list(GKAuthentication.LIST_FACE_PATH)).thenReturn(fakeResponse);
        GKAuthentication.ListTemplatesResult listTemplatesResult = new GKAuthentication(fakeCard).listTemplates();
        assertThat(listTemplatesResult.getTemplates(), is(empty()));
    }
}
