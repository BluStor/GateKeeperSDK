package co.blustor.gatekeeper.biometrics;

import android.content.Context;
import android.os.AsyncTask;

import com.neurotec.lang.NCore;
import com.neurotec.plugins.NDataFileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeeper.R;

/**
 * GKEnvironment ensures that the appropriate state is present for GateKeeper biometrics
 * to perform.
 *
 * @since 0.5.0
 */
public class GKEnvironment {
    public static final String TAG = GKEnvironment.class.getSimpleName();

    private static GKEnvironment mInstance;
    private final Context mContext;
    private final GKLicensing mLicensing;

    private GKEnvironment(Context context) {
        mContext = context;
        mLicensing = new GKLicensing("/local", 5000);
    }

    /**
     * Retrieve the {@code GKEnvironment} as a Singleton.
     *
     * @param context a valid Android {@code Context}
     * @return the {@code GKEnvironment} Singleton
     * @since 0.5.0
     */
    public static GKEnvironment getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GKEnvironment(context);
        }
        return mInstance;
    }

    /**
     * Begin establishing the state required by GateKeeper biometrics.
     *
     * @param listener an {@code InitializationListener} to be notified
     *                 upon successful initialization
     * @return the {@code AsyncTask} performing the initialization
     * @since 0.5.0
     */
    public AsyncTask<Void, Void, Void> initialize(final InitializationListener listener) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ensureDataFilesExist();
                NCore.setContext(mContext);
                mLicensing.obtainLicenses();
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                listener.onLicensesObtained();
            }
        };
        asyncTask.execute();
        return asyncTask;
    }

    /**
     * An InitializationListener will be notified when GateKeeper biometrics are ready
     * to be used.
     *
     * @since 0.5.0
     */
    public interface InitializationListener {
        void onLicensesObtained();
    }

    private void ensureDataFilesExist() {
        File facesFile = new File(mContext.getFilesDir(), "Faces.ndf");
        if (!facesFile.exists()) {
            try {
                InputStream is = mContext.getResources().openRawResource(R.raw.faces);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(facesFile);
                fos.write(buffer);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        NDataFileManager.getInstance().addFile(facesFile.getAbsolutePath());
    }
}
