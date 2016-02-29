package co.blustor.gatekeepersdk.biometrics;

import java.io.IOException;
import java.io.InputStream;

public interface GKBiometricClient {
    GKFaces.Template createTemplateFromStream(InputStream inputStream) throws IOException;
}
