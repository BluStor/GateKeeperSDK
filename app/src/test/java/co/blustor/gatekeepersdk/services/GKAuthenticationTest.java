package co.blustor.gatekeepersdk.services;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.blustor.gatekeepersdk.biometrics.GKFaces;
import co.blustor.gatekeepersdk.devices.GKCard;

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
    public void revokeRecoveryCodeRetunsSuccessWhenAuthenticatedTest() throws Exception {
        when(fakeCard.delete(GKAuthentication.ENROLL_RECOVERY_CODE_PATH_PREFIX + "1"))
                .thenReturn(new GKCard.Response(250, "RecoveryCode Unset"));

        GKAuthentication.AuthResult auth = new GKAuthentication(fakeCard).revokeRecoveryCode("1");

        assertThat(auth.getStatus(), is(equalTo(GKAuthentication.Status.SUCCESS)));
    }

    @Test
    public void revokeRecoveryCodeReturnsUnauthenticatedTest() throws Exception {
        when(fakeCard.delete(GKAuthentication.ENROLL_RECOVERY_CODE_PATH_PREFIX + "1"))
                .thenReturn(new GKCard.Response(530, "Unauthorized"));

        GKAuthentication.AuthResult auth = new GKAuthentication(fakeCard).revokeRecoveryCode("1");

        assertThat(auth.getStatus(), is(equalTo(GKAuthentication.Status.UNAUTHORIZED)));
    }

    @Test
    public void enrollWithRecoveryCodeReturnsSuccessForSuccessfulPut() throws Exception {
        GKCard.Response successResponse = new GKCard.Response(226, "success");
        when(fakeCard.put(eq(GKAuthentication.ENROLL_RECOVERY_CODE_PATH_PREFIX + "1"), any(InputStream.class)))
                .thenReturn(successResponse);
        when(fakeCard.finalize(GKAuthentication.ENROLL_RECOVERY_CODE_PATH_PREFIX + "1"))
                .thenReturn(successResponse);

        GKAuthentication.AuthResult result = new GKAuthentication(fakeCard).enrollWithRecoveryCode("1234", "1");

        verify(fakeCard).finalize(GKAuthentication.ENROLL_RECOVERY_CODE_PATH_PREFIX + "1");
        assertThat(result.getStatus(), is(equalTo(GKAuthentication.Status.SUCCESS)));
    }

    @Test
    public void enrollWithRecoveryCodeReturnsFailureWhenPutResponseStatusIsNot226() throws Exception {
        when(fakeCard.put(eq(GKAuthentication.ENROLL_RECOVERY_CODE_PATH_PREFIX + 1), any(InputStream.class)))
                .thenReturn(new GKCard.Response(426, "success"));

        GKAuthentication.AuthResult result = new GKAuthentication(fakeCard).enrollWithRecoveryCode("1234", "1");

        assertThat(result.getStatus(), is(equalTo(GKAuthentication.Status.CANCELED)));
    }

    @Test
    public void signInWithRecoveryCodeReturnsSuccessForSuccessfulPut() throws Exception {
        GKCard.Response successResponse = new GKCard.Response(226, "success");
        when(fakeCard.put(eq(GKAuthentication.SIGN_IN_RECOVERY_CODE_PATH), any(InputStream.class)))
                .thenReturn(successResponse);
        when(fakeCard.finalize(GKAuthentication.SIGN_IN_RECOVERY_CODE_PATH))
                .thenReturn(successResponse);

        GKAuthentication.AuthResult result = new GKAuthentication(fakeCard).signInWithRecoveryCode("1234");

        verify(fakeCard).finalize(GKAuthentication.SIGN_IN_RECOVERY_CODE_PATH);
        assertThat(result.getStatus(), is(equalTo(GKAuthentication.Status.SUCCESS)));
    }

    @Test
    public void returnsUnknownTemplateWhenNotAuthenticated() throws IOException {
        when(fakeCard.list(GKAuthentication.LIST_FACE_PATH))
                .thenReturn(new GKCard.Response(530, "Unauthorized"));
        GKAuthentication.ListTemplatesResult listTemplatesResult = new GKAuthentication(fakeCard).listFaceTemplates();
        assertThat(listTemplatesResult.getTemplates(), hasItem(equalTo(GKAuthentication.ListTemplatesResult.UNKNOWN_TEMPLATE)));
    }

    @Test
    public void listFaceTemplatesResultReturnsAllTemplates() throws IOException {
        GKCard.Response fakeResponse = mock(GKCard.Response.class);
        String data = "-rw-rw-rw-   1 root  root       4026 Dec 16  2015 1\r\n";
        when(fakeResponse.getData()).thenReturn(data.getBytes(StandardCharsets.UTF_8));
        when(fakeCard.list(GKAuthentication.LIST_FACE_PATH)).thenReturn(fakeResponse);
        GKAuthentication.ListTemplatesResult listTemplatesResult = new GKAuthentication(fakeCard).listFaceTemplates();
        assertThat(listTemplatesResult.getTemplates(), hasItem(equalTo("1")));
    }

    @Test
    public void returnsEmptyListWhenGetDataIsNull() throws IOException {
        GKCard.Response fakeResponse = mock(GKCard.Response.class);
        when(fakeResponse.getData()).thenReturn(null);
        when(fakeCard.list(GKAuthentication.LIST_FACE_PATH)).thenReturn(fakeResponse);
        GKAuthentication.ListTemplatesResult listTemplatesResult = new GKAuthentication(fakeCard).listFaceTemplates();
        assertThat(listTemplatesResult.getTemplates(), is(empty()));
    }

    @Test
    public void enrollWithFaceReturnsBadTemplateWhenTemplateQualityIsNotOK() throws IOException {
        GKFaces.Template fakeTemplate = mock(GKFaces.Template.class);
        when(fakeTemplate.getQuality()).thenReturn(GKFaces.Template.Quality.BAD_DATA);

        GKAuthentication.AuthResult result = new GKAuthentication(fakeCard).enrollWithFace(fakeTemplate, "template_1");

        GKAuthentication.AuthResult authResult = new GKAuthentication.AuthResult(GKAuthentication.Status.BAD_TEMPLATE);
        assertThat(result.getStatus(), is(equalTo(authResult.getStatus())));
    }

    @Test
    public void enrollWithFaceReturnsTemplateAddedWhenQualityIsOK() throws IOException {
        GKFaces.Template fakeTemplate = mock(GKFaces.Template.class);
        when(fakeTemplate.getQuality()).thenReturn(GKFaces.Template.Quality.OK);
        InputStream inputStream = mock(InputStream.class);
        when(fakeTemplate.getInputStream()).thenReturn(inputStream);
        when(fakeCard.put(Mockito.anyString(), Mockito.eq(inputStream))).thenReturn(new GKCard.Response(213, "template Added"));

        GKAuthentication.AuthResult result = new GKAuthentication(fakeCard).enrollWithFace(fakeTemplate, "template_1");

        GKAuthentication.AuthResult authResult = new GKAuthentication.AuthResult(GKAuthentication.Status.TEMPLATE_ADDED);
        assertThat(result.getStatus(), is(equalTo(authResult.getStatus())));
    }

    @Test
    public void revokeFaceReturnsSuccessWhenDeletingATemplate() throws IOException {
        String templateId = "0";
        GKAuthentication authentication = new GKAuthentication(fakeCard);
        GKCard.Response successResponse = new GKCard.Response(226, "success");
        when(fakeCard.delete(Mockito.anyString())).thenReturn(successResponse);

        GKAuthentication.AuthResult authResult = authentication.revokeFace("0");

        verify(fakeCard).delete(GKAuthentication.REVOKE_FACE_PATH_PREFIX + templateId);
        GKAuthentication.AuthResult successStatus = new GKAuthentication.AuthResult(successResponse);
        assertThat(authResult.getStatus(), is(equalTo(successStatus.getStatus())));
    }
}
