package edu.mit.mobile.android.livingpostcards.app;


import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stackoverflow.ArrayUtils;

import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.livingpostcards.R;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.locast.maps.LocatableMapFragment;

public class CardMapFragment extends LocatableMapFragment {

    public static String[] CARD_PROJECTION = ArrayUtils.concat(LocatableMapFragment.PROJECTION,
            new String[] { Card.COL_THUMBNAIL, Card.COL_PRIVACY });

    public static final String ARG_SHOW_MY_LOCATION = "edu.mit.mobile.android.livingpostcards.app.ARG_SHOW_MY_LOCATION";

    public static CardMapFragment instantiate(Uri cardDir, boolean showMyLocation) {
        final Bundle args = new Bundle(1);

        args.putParcelable(ARG_LOCATABLE_DIR, cardDir);
        args.putBoolean(ARG_SHOW_MY_LOCATION, showMyLocation);

        final CardMapFragment f = new CardMapFragment();

        f.setArguments(args);

        return f;
    }

    private ImageCache mImageCache;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImageCache = ImageCache.getInstance(getActivity());

        final boolean showMyLocation = getArguments().getBoolean(ARG_SHOW_MY_LOCATION, false);
        setShowMyLocation(showMyLocation);

        final GoogleMap map = getMap();

        if (map != null && showMyLocation) {
            final LocationManager lm = (LocationManager) getActivity().getSystemService(
                    Context.LOCATION_SERVICE);
            final Location myLoc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            // XXX this returns null
            // final Location myLoc = getMap().getMyLocation();
            if (myLoc != null) {
                map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                new LatLng(myLoc.getLatitude(), myLoc.getLongitude()), 16));
            }
        }
    }

    @Override
    protected String[] getProjection() {
        return CARD_PROJECTION;
    }

    @Override
    protected MarkerOptions getMarker(Cursor c) {
        final MarkerOptions mo = super.getMarker(c);

        final boolean isCollaborative = Card.isCollaborative(c);
        mo.icon(BitmapDescriptorFactory
                .fromResource(isCollaborative ? R.drawable.ic_map_marker_collaborative
                        : R.drawable.ic_map_marker_normal));

        return mo;
    }
}
