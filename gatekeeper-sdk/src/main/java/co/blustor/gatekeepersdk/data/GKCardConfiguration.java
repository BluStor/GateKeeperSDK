package co.blustor.gatekeepersdk.data;

import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the data stored under /device/settings, which contain configurable values for card operations
 */
public class GKCardConfiguration {
    private static final String TAG = GKCardConfiguration.class.getCanonicalName();
    private static final Type CONFIG_TYPE = new TypeToken<Map<String, Integer>>() {}.getType();
    private static final String AUTHENTICATION_TO = "authentication_to";
    /**
     * The config values, parsed from JSON, that were retrieved from /device/settings endpoint
     */
    protected final Map<String, Integer> mConfigValues;

    public GKCardConfiguration(String configValues) {
        mConfigValues = parseConfigValues(configValues);
    }

    public String getConfigJson() {
        return new GsonBuilder().create().toJson(mConfigValues, CONFIG_TYPE);
    }

    /**
     * Get the timeout in seconds the card will wait until terminating a session once the SPP connection is terminated
     *
     * @return Integer representing the timeout in seconds or null if not present
     * @since 0.17.0
     */
    public Integer getAuthTimeout() {
        return mConfigValues.get(AUTHENTICATION_TO);
    }

    /**
     * Set the timeout in seconds the card will wait until terminating a session once the SPP connection is terminated. Removes the key if value is null.
     *
     * @param authTimeout the value with which to set the timeout
     * @since 0.17.0
     */
    public void setAuthTimeout(Integer authTimeout) {
        setValue(AUTHENTICATION_TO, authTimeout);
    }

    private void setValue(String key, Integer value) {
        if (value == null) {
            mConfigValues.remove(key);
        } else {
            mConfigValues.put(key, value);
        }
    }

    private Map<String, Integer> parseConfigValues(String configValues) {
        try {
            if (configValues != null && !configValues.isEmpty()) {
                return new GsonBuilder().create().fromJson(configValues, CONFIG_TYPE);
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Error parsing card settings", e);
        }
        return new HashMap<>();
    }
}
