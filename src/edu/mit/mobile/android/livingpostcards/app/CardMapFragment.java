package edu.mit.mobile.android.livingpostcards.app;

import java.io.IOException;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stackoverflow.ArrayUtils;

import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageCache.OnImageLoadListener;
import edu.mit.mobile.android.livingpostcards.R;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.locast.maps.LocatableMapFragment;

public class CardMapFragment extends LocatableMapFragment {

    public static String[] CARD_PROJECTION = ArrayUtils.concat(LocatableMapFragment.PROJECTION,
            new String[] { Card.COL_THUMBNAIL, Card.COL_PRIVACY });

    public static final String ARG_SHOW_MY_LOCATION = "edu.mit.mobile.android.livingpostcards.app.ARG_SHOW_MY_LOCATION";

    private static final String TAG = CardMapFragment.class.getSimpleName();

    public static CardMapFragment instantiate(Uri cardDir, boolean showMyLocation) {
        final Bundle args = new Bundle(1);

        args.putParcelable(ARG_LOCATABLE_DIR, cardDir);
        args.putBoolean(ARG_SHOW_MY_LOCATION, showMyLocation);

        final CardMapFragment f = new CardMapFragment();

        f.setArguments(args);

        return f;
    }

    private ImageCache mImageCache;

    private CardInfoWindowAdapter mInfoWindowAdapter;

    private final OnImageLoadListener mOnImageLoadListener = new OnImageLoadListener() {

        @Override
        public void onImageLoaded(long id, Uri imageUri, Drawable image) {

        }

        @Override
        public void onImageLoaded(int id, Uri imageUri, Drawable image) {
            final Marker m = mPendingLoads.get(id);

            if (m != null) {
                m.showInfoWindow();
            }
        }
    };

    private void clearPendingLoads() {
        mPendingLoads.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageCache = ImageCache.getInstance(getActivity());
    };

    @Override
    public void onPause() {
        super.onPause();

        mImageCache.unregisterOnImageLoadListener(mOnImageLoadListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        mImageCache.registerOnImageLoadListener(mOnImageLoadListener);
    };

    private final SparseArray<Marker> mPendingLoads = new SparseArray<Marker>();

    private Drawable loadMarkerImage(Marker marker, Uri image) {
        final int id = mImageCache.getNewID();

        Drawable imageDrawable = null;
        try {
            imageDrawable = mImageCache.loadImage(id, image, 320, 240);
            if (imageDrawable == null) {
                mPendingLoads.put(id, marker);
            }
        } catch (final IOException e) {
            Log.e(TAG, "error loading image", e);
        }

        return imageDrawable;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImageCache = ImageCache.getInstance(getActivity());

        final boolean showMyLocation = getArguments().getBoolean(ARG_SHOW_MY_LOCATION, false);
        setShowMyLocation(showMyLocation);

        final GoogleMap map = getMap();

        // bail if there's no map
        if (map == null) {
            return;
        }

        mInfoWindowAdapter = new CardInfoWindowAdapter(getActivity(), this);
        map.setInfoWindowAdapter(mInfoWindowAdapter);

        // the map doesn't automatically snap to our current location, so we need to do that
        // somehow.
        if (showMyLocation) {
            final LocationManager lm = (LocationManager) getActivity().getSystemService(
                    Context.LOCATION_SERVICE);
            final Location myLoc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            // XXX this returns null
            // final Location myLoc = getMap().getMyLocation();
            if (myLoc != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLoc.getLatitude(),
                        myLoc.getLongitude()), 16));
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

    public static class CardInfoWindowAdapter implements InfoWindowAdapter {

        private static final String[] PROJECTION_INFO_WINDOW = new String[] { Card.COL_THUMBNAIL,
                Card.COL_COVER_PHOTO, Card.COL_PRIVACY, Card.COL_AUTHOR };
        private final CardMapFragment mCardMapFragment;
        private final View mContent;
        private final ContentResolver mCr;
        private final Context mContext;

        public CardInfoWindowAdapter(Context context, CardMapFragment cmf) {
            mCardMapFragment = cmf;
            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContent = inflater.inflate(R.layout.card_map_info_window, null);
            mCr = context.getContentResolver();
            mContext = context.getApplicationContext();
        }

        @Override
        public View getInfoContents(Marker marker) {
            final View v = mContent;

            mCardMapFragment.clearPendingLoads();

            final Uri item = mCardMapFragment.getItem(marker);
            final Cursor c = mCr.query(item, PROJECTION_INFO_WINDOW, null, null, null);

            try {
                String thumb;

                boolean isCollaborative = false;

                if (c.moveToFirst()) {
                    isCollaborative = Card.isCollaborative(c);
                    thumb = c.getString(c.getColumnIndex(Card.COL_COVER_PHOTO));
                    if (thumb == null) {
                        thumb = c.getString(c.getColumnIndex(Card.COL_THUMBNAIL));
                    }

                    if (thumb != null) {
                        final Drawable d = mCardMapFragment.loadMarkerImage(marker,
                                Uri.parse(thumb));

                        final ImageView imgView = (ImageView) v
                                .findViewById(R.id.card_media_thumbnail);
                        if (d != null) {
                            imgView.setImageDrawable(d);
                        } else {
                            imgView.setImageResource(R.drawable.image_placeholder);
                        }
                    }

                    ((TextView) v.findViewById(R.id.author)).setText(c.getString(c
                            .getColumnIndex(Card.COL_AUTHOR)));
                }
            } finally {
                c.close();
            }

            ((TextView) v.findViewById(R.id.title)).setText(marker.getTitle());

            return v;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

    }
}
