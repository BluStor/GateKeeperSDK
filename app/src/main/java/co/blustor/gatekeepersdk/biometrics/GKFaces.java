package co.blustor.gatekeepersdk.biometrics;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

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
    public static final String FRONT_CAMERA_DEVICE_NAME = "Front";
    private final NBiometricClient mBiometricClient;

    /**
     * Create a {@code GKFaces} instance.
     */
    public GKFaces() {
        mBiometricClient = new NBiometricClient();
        mBiometricClient.setFacesTemplateSize(NTemplateSize.SMALL);
        mBiometricClient.setUseDeviceManager(true);
        mBiometricClient.initialize();
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
     * @throws IOException when a template fails to be created
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
     * Set up view for face capturing
     *
     * @param context   application/activity context.
     * @param container ViewGroup where the camera is going to be displayed
     * @return the attached {@code NFaceView}
     * @since 0.13.0
     */
    public NFaceView setFaceCaptureView(Context context, ViewGroup container) {
        NFaceView nFaceView = new NFaceView(context);
        nFaceView.setVisibility(View.VISIBLE);
        container.addView(nFaceView);
        return nFaceView;
    }

    /**
     * Start the camera for face capturing
     *
     * @param faceView the {@code View} where the camera will be displayed
     * @param listener {@code OnCameraCompletionListener} for success/failure responses
     * @since 0.13.0
     */
    public void startCapturing(NFaceView faceView, final OnCameraCompletionListener listener) {
        NSubject nSubject = new NSubject();
        NFace nFace = new NFace();
        setFrontFaceCamera();
        EnumSet<NBiometricCaptureOption> options = EnumSet.of(NBiometricCaptureOption.MANUAL, NBiometricCaptureOption.STREAM);
        nFace.setCaptureOptions(options);
        faceView.setFace(nFace);
        nSubject.getFaces().add(nFace);
        startCameraCapture(nSubject, new CompletionHandler<NBiometricTask, NBiometricOperation>() {
            @Override
            public void completed(NBiometricTask nBiometricTask, NBiometricOperation nBiometricOperation) {
                NImage image = getNImage(nBiometricTask);
                if (image != null) {
                    GKFaces.Template template = createTemplateFromNImage(image);
                    listener.onSuccess(template, image.toBitmap());
                }
            }

            @Override
            public void failed(Throwable throwable, NBiometricOperation nBiometricOperation) {
                // this is called too often and too generically for us to have a use for it
            }
        });
    }

    /**
     * Captures results from the current camera thread when thread is active
     * No-op when there is no camera capture thread
     *
     * @since 0.13.0
     */
    public void captureImage() {
        mBiometricClient.force();
    }

    /**
     * Cancels the current camera capture thread
     * No-op when there is no camera capture thread
     *
     * @since 0.13.0
     */
    public void finishCameraCapture() {
        mBiometricClient.cancel();
    }

    private NImage getNImage(NBiometricTask nBiometricTask) {
        NBiometricTask.SubjectCollection subjects = nBiometricTask.getSubjects();
        if (subjects.isEmpty()) {
            return null;
        }
        NSubject.FaceCollection faces = subjects.get(0).getFaces();
        if (faces.isEmpty()) {
            return null;
        }

        return faces.get(0).getImage();
    }

    private void setFrontFaceCamera() {
        for (NDevice device : mBiometricClient.getDeviceManager().getDevices()) {
            if (device.getDeviceType().contains(NDeviceType.CAMERA)) {
                if (device.getDisplayName().contains(FRONT_CAMERA_DEVICE_NAME)) {
                    if (!mBiometricClient.getFaceCaptureDevice().equals(device))
                        mBiometricClient.setFaceCaptureDevice((NCamera) device);
                }
            }
        }
    }

    private void startCameraCapture(NSubject nSubject, CompletionHandler completionHandler) {
        NBiometricTask task = mBiometricClient.createTask(EnumSet.of(NBiometricOperation.CREATE_TEMPLATE), nSubject);
        mBiometricClient.performTask(task, NBiometricOperation.CREATE_TEMPLATE, completionHandler);
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
     * Interface of camera completion results
     * <p>
     * when successful onSuccess will provide a {@code Template}
     *
     * @since 0.13.0
     */
    public interface OnCameraCompletionListener {
        void onSuccess(Template template, Bitmap image);
    }

    /**
     * A Template contains captured facial biometric data.
     */
    public static class Template {
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
    }
}
