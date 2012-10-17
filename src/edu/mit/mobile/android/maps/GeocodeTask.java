package edu.mit.mobile.android.maps;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import edu.mit.mobile.android.livingpostcards.R;
import edu.mit.mobile.android.utils.AddressUtils;

public class GeocodeTask extends AsyncTask<Location, Void, String> {
    private final Context mContext;
    private final TextView mTextView;
    private static final String TAG = GeocodeTask.class.getSimpleName();

    public GeocodeTask(Context context, TextView output) {
        mContext = context;
        mTextView = output;
    }

    @Override
    protected String doInBackground(Location... params) {

        final Geocoder geocoder = new Geocoder(mContext);

        final Location location = params[0];

        try {
            List<Address> addresses;
            addresses = geocoder
                    .getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses.size() > 0) {
                return AddressUtils.addressToName(addresses.get(0));
            }
        } catch (final IOException e) {
            Log.e(TAG, "error looking up location", e);
        }
        return mContext.getString(R.string.geocoder_lookup_unknown_location);
    }

    @Override
    protected void onPostExecute(String result) {

        mTextView.setText(result);
    }
}