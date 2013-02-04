package edu.mit.mobile.android.maps;
/*
 * Copyright (C) 2012-2013  MIT Mobile Experience Lab
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
