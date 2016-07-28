package co.blustor.gatekeepersdk.biometrics;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.neurotec.lang.NCore;
import com.neurotec.plugins.NDataFileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.blustor.gatekeepersdk.R;
import co.blustor.gatekeepersdk.services.GKFileActions;

/**
 * GKEnvironment ensures that the appropriate state is present for GateKeeper biometrics
 * to perform.
 *
 * @since 0.5.0
 */
public class GKEnvironment {
    public static final String TAG = GKEnvironment.class.getCanonicalName();

    private static GKEnvironment mInstance;
    private final Context mContext;
    private final GKLicensing mLicensing;

    private GKEnvironment(Context context, GKFileActions fileActions) {
        mContext = context;
        mLicensing = buildLicensing(context, fileActions);
    }

    /**
     * Retrieve the {@code GKEnvironment} as a Singleton.
     *
     * @param context     a valid Android {@code Context}
     * @param fileActions a {@code GKFileActions} to fetch licenses from the card
     * @return the {@code GKEnvironment} Singleton
     * @since 0.11.0
     */
    public static GKEnvironment getInstance(Context context, GKFileActions fileActions) {
        if (mInstance == null) {
            mInstance = new GKEnvironment(context, fileActions);
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
    public AsyncTask<Void, Void, GKLicenseValidationResult> initialize(final InitializationListener listener) {
        Log.d(TAG, "initialize()");
        AsyncTask<Void, Void, GKLicenseValidationResult> asyncTask = new AsyncTask<Void, Void, GKLicenseValidationResult>() {
            @Override
            protected GKLicenseValidationResult doInBackground(Void... params) {
                Log.d(TAG, "doInBackground()");
                ensureDataFilesExist();
                NCore.setContext(mContext);
                return mLicensing.obtainLicenses();
            }

            @Override
            protected void onPostExecute(GKLicenseValidationResult result) {
                super.onPostExecute(result);
                switch (result) {
                    case SUCCESS:
                        listener.onLicensesObtained();
                        break;
                    case NO_LICENSES_AVAILABLE:
                        listener.onNoLicensesAvailable();
                        break;
                    case VALIDATION_FAILURE:
                        listener.onLicenseValidationFailure();
                        break;
                    case ERROR:
                        listener.onLicenseValidationError();
                        break;
                }
            }
        };
        asyncTask.execute();
        return asyncTask;
    }

    @NonNull
    private GKLicensing buildLicensing(Context context, GKFileActions fileActions) {

        //String macAddress = getMacAddress(context);
        String macAddress = android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address");
        Log.d(TAG, "buildLicensing(): macAddress = " + macAddress);
        BiometricLicenseManager licenseManager = new BiometricLicenseManager("/local", 5000);
        return new GKLicensing(macAddress, fileActions, licenseManager);
    }

    @Nullable
    private String getMacAddress(Context context) {
        WifiManager wifiService = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiService.getConnectionInfo();
        Log.d(TAG, "getMacAddress(): connectionInfo = " + connectionInfo);
        return connectionInfo == null ? null : connectionInfo.getMacAddress();
    }

    private void ensureDataFilesExist() {
        Log.d(TAG, "ensureDataFilesExist()");
        Log.d(TAG, "ensureDataFilesExist(): mContext.getFilesDir() = " + mContext.getFilesDir());
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
        Log.d(TAG, "ensureDataFilesExist(): Add file to Data File Manager");
        NDataFileManager.getInstance().addFile(facesFile.getAbsolutePath());
    }

    /**
     * An InitializationListener will be notified when GateKeeper biometrics are ready
     * to be used, or if license validation has failed
     *
     * @since 0.11.0
     */
    public interface InitializationListener {
        void onLicensesObtained();
        void onNoLicensesAvailable();
        void onLicenseValidationFailure();
        void onLicenseValidationError();
    }
}
