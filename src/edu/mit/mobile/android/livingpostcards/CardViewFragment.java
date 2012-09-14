package edu.mit.mobile.android.livingpostcards;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageCache.OnImageLoadListener;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.maps.GoogleStaticMaps;

/**
 * Views a single card.
 *
 */
public class CardViewFragment extends Fragment implements LoaderCallbacks<Cursor>,
        OnImageLoadListener {

    /**
     * The card URI
     */
    public static final String ARGUMENT_URI = "uri";

    private ImageCache mImageCache;

    private Uri mUri;

    private ImageView mCardImage;

    private ImageView mStaticMap;

    private GoogleStaticMaps mStaticMapUtil;

    public static CardViewFragment newInstance(Uri card) {
        final CardViewFragment cmf = new CardViewFragment();

        final Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_URI, card);
        cmf.setArguments(args);
        return cmf;
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        mImageCache = ImageCache.getInstance(activity);

        mStaticMapUtil = new GoogleStaticMaps(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUri = getArguments().getParcelable(ARGUMENT_URI);

            if (mUri != null) {
                getLoaderManager().initLoader(0, null, this);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.card_view_fragment, container, false);

        mCardImage = (ImageView) v.findViewById(R.id.card_image);
        mStaticMap = (ImageView) v.findViewById(R.id.static_map);

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();

        mImageCache.unregisterOnImageLoadListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageCache.registerOnImageLoadListener(this);

    }

    private static final String[] PROJECTION = new String[] { Card._ID, Card.COL_ANIMATED_RENDER,
            Card.COL_TITLE, Card.COL_DESCRIPTION, Card.COL_COVER_PHOTO, Card.COL_AUTHOR,
            Card.COL_LATITUDE, Card.COL_LONGITUDE };
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

        return new CursorLoader(getActivity(), mUri, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        if (c.moveToFirst()) {
            final String cardPoster = c.getString(c.getColumnIndex(Card.COL_COVER_PHOTO));
            if (cardPoster != null) {

                mImageCache.scheduleLoadImage(R.id.card_image, Uri.parse(cardPoster), 640, 480);
            }
            getActivity().setTitle(c.getString(c.getColumnIndex(Card.COL_TITLE)));
            final View v = getView();
            ((TextView) v.findViewById(R.id.description)).setText(c.getString(c
                    .getColumnIndexOrThrow(Card.COL_DESCRIPTION)));
            ((TextView) v.findViewById(R.id.author)).setText(c.getString(c
                    .getColumnIndexOrThrow(Card.COL_AUTHOR)));

            final Uri staticMap = mStaticMapUtil.getMap(
                    c.getFloat(c.getColumnIndexOrThrow(Card.COL_LATITUDE)),
                    c.getFloat(c.getColumnIndexOrThrow(Card.COL_LONGITUDE)), 640, 200, false);
            mImageCache.scheduleLoadImage(R.id.static_map, staticMap, 640, 200);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onImageLoaded(long id, Uri imageUri, Drawable image) {
        if (id == R.id.card_image) {
            mCardImage.setImageDrawable(image);
        } else if (id == R.id.static_map) {
            mStaticMap.setImageDrawable(image);
        }
    }
}
