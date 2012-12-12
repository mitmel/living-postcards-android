package edu.mit.mobile.android.locast.maps;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.maps.SupportMapFragment;

import edu.mit.mobile.android.locast.data.Locatable;

public class LocatableMapFragment extends SupportMapFragment {

    /**
     * Pass this a {@link Uri} of the dir of some items that are {@link Locatable}.
     */
    public static final String ARG_LOCATABLE_DIR = "edu.mit.mobile.android.locast.maps.ARG_LOCATABLE_DIR";

    public static final LocatableMapFragment getInstance(Uri locatableDir) {

        final LocatableMapFragment f = new LocatableMapFragment();

        final Bundle args = new Bundle(1);

        args.putParcelable(ARG_LOCATABLE_DIR, locatableDir);

        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
