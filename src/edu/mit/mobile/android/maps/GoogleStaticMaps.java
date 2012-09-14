package edu.mit.mobile.android.maps;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;

/**
 * Constructs URLs that load static images of a given location. To use, add your Google Static Maps
 * API key (this is <em>not</em> a normal Google Maps API key) to your Android manifest in the
 * &lt;application /&gt; section:
 *
 * <pre>
 *         &lt;meta-data
 *             android:name="edu.mit.mobile.android.maps.GOOGLE_STATIC_MAPS_API_KEY"
 *             android:value="AAeeSUeOKuhEuEu3eu43E2ue#jueEEuuU3hwm0" /&gt;
 *
 * </pre>
 *
 */
public class GoogleStaticMaps {

    private final String mKey;

    public static final String METADATA_KEY_API_KEY = "edu.mit.mobile.android.maps.GOOGLE_STATIC_MAPS_API_KEY";

    private final static Uri BASE_URL = Uri.parse("https://maps.googleapis.com/maps/api/staticmap");

    /**
     * Constructs URLs that load static images of a given location.
     *
     * @param context
     */
    public GoogleStaticMaps(Context context) {
        final PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo;
        try {
            appInfo = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (final NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        final Bundle metadata = appInfo.metaData;
        mKey = metadata.getString(METADATA_KEY_API_KEY);
    }

    public Uri getMap(float latitude, float longitude, int width, int height, boolean sensor) {
        final Builder b = BASE_URL.buildUpon();
        b.appendQueryParameter("key", mKey);
        b.appendQueryParameter("size", width + "x" + height);
        b.appendQueryParameter("zoom", "13");
        b.appendQueryParameter("scale", "2");
        b.appendQueryParameter("sensor", sensor ? "true" : "false");
        b.appendQueryParameter("center", latitude + "," + longitude);
        return b.build();
    }
}
