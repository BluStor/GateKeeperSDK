package co.blustor.gatekeeper.biometrics;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;

import com.neurotec.biometrics.NBiometric;
import com.neurotec.biometrics.NBiometricCaptureOption;
import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NLRecord;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplate;
import com.neurotec.biometrics.NTemplateSize;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.view.NFaceView;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceType;
import com.neurotec.images.NImage;
import com.neurotec.util.concurrent.CompletionHandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.EnumSet;

/**
 * GKFaces is a Service for handling facial biometrics.
 */
public class GKFaces {
    private final NBiometricClient mBiometricClient;

    /**
     * Create a {@code GKFaces} instance.
     */
    public GKFaces() {
        mBiometricClient = new NBiometricClient();
        mBiometricClient.setUseDeviceManager(true);
        mBiometricClient.initialize();
        mBiometricClient.setFacesTemplateSize(NTemplateSize.SMALL);
    }

    /**
     * Extract a face {@code Template} from the given {@code Bitmap}.
     *
     * @param bitmap a {@code Bitmap} image of a face to extract
     * @return a {@code Template} with facial capture data
     * @since 0.5.0
     */
    public Template createTemplateFromBitmap(Bitmap bitmap) {
        NImage nImage = NImage.fromBitmap(bitmap);
        return createTemplateFromNImage(nImage);
    }

    /**
     * Extract a face {@code Template} from the given image {@code File}.
     *
     * @param file an image {@code File} of a face to extract
     * @return a {@code Template} with facial capture data
     * @since 0.6.0
     */
    public Template createTemplateFromImage(File file) throws IOException {
        NImage nImage = NImage.fromFile(file.getCanonicalPath());
        return createTemplateFromNImage(nImage);
    }

    /**
     * Retrieve a face {@code Template} from the given {@code InputStream}.
     *
     * @param inputStream stream data of a capture {@code Template}
     * @return a {@code Template} created from the stream data
     * @throws IOException when the stream could not be read successfully
     * @since 0.5.0
     */
    public Template createTemplateFromStream(InputStream inputStream) throws IOException {
        try {
            byte[] bytes = getTemplateBytes(inputStream);
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            NSubject subject = NSubject.fromMemory(byteBuffer);
            return new Template(subject, NBiometricStatus.OK);
        } catch (UnsupportedOperationException e) {
            return new Template(Template.Quality.BAD_DATA);
        }
    }

    /**
     * Set up device front camera for capture
     *
     * @param context application/activity context.
     * @param container LinearLayout where the camera is going to be displayed
     * @param listener {@code OnCameraCompletionListener} for success/failure responses
     * @return Void
     * @since 0.6.1
     */
    public void setupCamera(Context context, LinearLayout container, OnCameraCompletionListener listener) {
        NFaceView nFaceView = new NFaceView(context);
        nFaceView.setVisibility(View.VISIBLE);
        container.addView(nFaceView);
        startCapturing(nFaceView, listener);
    }

    private void startCapturing(NFaceView nFaceView, final OnCameraCompletionListener listener) {
        NSubject nSubject = new NSubject();
        NFace nFace = new NFace();
        nFace.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if ("Status".equals(event.getPropertyName())) {
                    ((NBiometric) event.getSource()).getStatus();
                }
            }
        });
        EnumSet<NBiometricCaptureOption> options = EnumSet.of(NBiometricCaptureOption.MANUAL);
        options.add(NBiometricCaptureOption.STREAM);
        setFrontFaceCamera();
        nFace.setCaptureOptions(options);
        nFaceView.setFace(nFace);
        nSubject.getFaces().add(nFace);
        startCameraCapture(nSubject, new CompletionHandler<NBiometricTask, NBiometricOperation>() {
            @Override
            public void completed(NBiometricTask nBiometricTask, NBiometricOperation nBiometricOperation) {
                NImage image = nBiometricTask.getSubjects().get(0).getFaces().get(0).getImage();
                GKFaces.Template template = createTemplateFromBitmap(image.toBitmap());
                listener.onSuccess(template);
            }

            @Override
            public void failed(Throwable throwable, NBiometricOperation nBiometricOperation) {
                listener.onFailure();
            }
        });
    }

    /**
     * Interface of camera completion results
     *
     * when successful onSuccess will provide a {@code Template}
     * @since 0.6.1
     */
    public interface OnCameraCompletionListener {
        Void onSuccess(Template template);
        Void onFailure();
    }

    private void setFrontFaceCamera() {
        for (NDevice device : mBiometricClient.getDeviceManager().getDevices()) {
            if (device.getDeviceType().contains(NDeviceType.CAMERA)) {
                if (device.getDisplayName().contains("Front")) {
                    if (!mBiometricClient.getFaceCaptureDevice().equals(device))
                        mBiometricClient.setFaceCaptureDevice((NCamera) device);
                }
            }
        }
    }

    private void startCameraCapture(NSubject nSubject, CompletionHandler completionHandler) {
        if (nSubject == null) throw new NullPointerException("subject");
        NBiometricTask task = mBiometricClient.createTask(EnumSet.of(NBiometricOperation.CREATE_TEMPLATE), nSubject);
        mBiometricClient.performTask(task, NBiometricOperation.CREATE_TEMPLATE, completionHandler);
    }

    /**
     * Captures results from the current camera thread when thread is active
     * No-op when there is no camera capture thread
     *
     * @return Void
     * @since 0.6.1
     */
    public void captureImage() {
        mBiometricClient.force();
    }

    /**
     * Cancels the current camera capture thread
     * No-op when there is no camera capture thread
     *
     * @return Void
     * @since 0.6.1
     */
    public void finishCameraCapture() {
        mBiometricClient.cancel();
    }

    private Template createTemplateFromNImage(NImage nImage) {
        NSubject subject = new NSubject();
        NFace nFace = mBiometricClient.detectFaces(nImage);
        nFace.setImage(nImage);
        subject.getFaces().add(nFace);
        NBiometricStatus status = mBiometricClient.createTemplate(subject);
        return new Template(subject, status);
    }

    private byte[] getTemplateBytes(InputStream stream) throws IOException {
        int bytesRead;
        int bufferByteCount = 1024;
        byte[] buffer;

        ByteArrayOutputStream outputByteStream = new ByteArrayOutputStream();
        buffer = new byte[bufferByteCount];
        while ((bytesRead = stream.read(buffer, 0, bufferByteCount)) != -1) {
            outputByteStream.write(buffer, 0, bytesRead);
        }
        return outputByteStream.toByteArray();
    }

    /**
     * A Template contains captured facial biometric data.
     */
    public static class Template {
        /**
         * Indicators of usability for captured images.
         */
        public enum Quality {
            /**
             * The Template was successfully created.
             */
            OK,

            /**
             * The image was not sharp enough to extract a Template.
             */
            BLURRY,

            /**
             * A face was not found within the captured image.
             */
            NO_FACE,

            /**
             * The source data could not be used to create a Template.
             */
            BAD_DATA
        }

        private final NSubject mSubject;
        private final Quality mQuality;

        private Template(Quality quality) {
            mSubject = null;
            mQuality = quality;
        }

        private Template(NSubject subject, NBiometricStatus biometricStatus) {
            mSubject = subject;
            mQuality = parseQuality(biometricStatus);
        }

        /**
         * Retrieve the {@code Quality} of the Template.
         *
         * @return the quality of the Template
         * @since 0.5.0
         */
        public Quality getQuality() {
            return mQuality;
        }

        /**
         * Convert the Template data to an {@code InputStream}.
         *
         * @return an {@code InputStream} to read the Template data
         * @since 0.5.0
         */
        @NonNull
        public InputStream getInputStream() {
            NTemplate template = null;
            try {
                template = mSubject.getTemplate();
                NLRecord faceRecord = template.getFaces().getRecords().get(0);
                byte[] buffer = faceRecord.save().toByteArray();
                return new ByteArrayInputStream(buffer);
            } catch (NullPointerException e) {
                return new ByteArrayInputStream(new byte[0]);
            } finally {
                if (template != null) {
                    template.dispose();
                }
            }
        }

        private Quality parseQuality(NBiometricStatus biometricStatus) {
            switch (biometricStatus) {
                case OK:
                    return Quality.OK;
                case BAD_SHARPNESS:
                    return Quality.BLURRY;
                default:
                    return Quality.NO_FACE;
            }
        }
    }
}
