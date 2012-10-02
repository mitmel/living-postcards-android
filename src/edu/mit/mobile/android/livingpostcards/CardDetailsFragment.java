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
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import edu.mit.mobile.android.content.ProviderUtils;
import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageCache.OnImageLoadListener;
import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.locast.net.NetworkClient;
import edu.mit.mobile.android.locast.sync.LocastSyncService;
import edu.mit.mobile.android.maps.GoogleStaticMaps;

public class CardDetailsFragment extends Fragment implements LoaderCallbacks<Cursor>,
        OnImageLoadListener {

    /**
     * The card URI
     */
    public static final String ARGUMENT_URI = "uri";

    private ImageCache mImageCache;

    private Uri mCard;

    private ImageView mStaticMap;

    private GoogleStaticMaps mStaticMapUtil;

    private int mTiming;

    private Uri mCardMedia;

    private final static int LOADER_CARD = 100;

    private static final String[] CARD_PROJECTION = new String[] { Card._ID,
            Card.COL_ANIMATED_RENDER, Card.COL_TITLE, Card.COL_DESCRIPTION, Card.COL_COVER_PHOTO,
            Card.COL_AUTHOR, Card.COL_TIMING, Card.COL_LATITUDE, Card.COL_LONGITUDE,
            Card.COL_MEDIA_URL };

    public static CardDetailsFragment newInstance(Uri card) {
        final CardDetailsFragment cmf = new CardDetailsFragment();

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
            mCard = getArguments().getParcelable(ARGUMENT_URI);

            if (mCard != null) {
                mCardMedia = Card.MEDIA.getUri(mCard);
                getLoaderManager().initLoader(LOADER_CARD, null, this);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.card_details_fragment, container, false);

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

        LocastSyncService.startExpeditedAutomaticSync(getActivity(), mCard);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        switch (id) {
            case LOADER_CARD:
                return new CursorLoader(getActivity(), mCard, CARD_PROJECTION, null, null, null);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        switch (loader.getId()) {
            case LOADER_CARD:
                if (c.moveToFirst()) {
                    if (BuildConfig.DEBUG) {
                        ProviderUtils.dumpCursorToLog(c, CARD_PROJECTION);
                    }
                    getActivity().setTitle(c.getString(c.getColumnIndex(Card.COL_TITLE)));
                    final View v = getView();
                    ((TextView) v.findViewById(R.id.description)).setText(c.getString(c
                            .getColumnIndexOrThrow(Card.COL_DESCRIPTION)));
                    ((TextView) v.findViewById(R.id.author)).setText(c.getString(c
                            .getColumnIndexOrThrow(Card.COL_AUTHOR)));

                    final Uri staticMap = mStaticMapUtil.getMap(
                            c.getFloat(c.getColumnIndexOrThrow(Card.COL_LATITUDE)),
                            c.getFloat(c.getColumnIndexOrThrow(Card.COL_LONGITUDE)), 640, 200,
                            false);
                    mImageCache.scheduleLoadImage(R.id.static_map, staticMap, 640, 200);
                    mTiming = c.getInt(c.getColumnIndexOrThrow(Card.COL_TIMING));
                    final String pubMediaUri = c.getString(c
                            .getColumnIndexOrThrow(Card.COL_MEDIA_URL));
                    if (pubMediaUri != null) {
                        LocastSyncService.startSync(
                                getActivity(),
                                NetworkClient.getInstance(
                                        getActivity(),
                                        Authenticator.getFirstAccount(getActivity(),
                                                Authenticator.ACCOUNT_TYPE))
                                        .getFullUrl(pubMediaUri), mCardMedia, false);
                    }
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onImageLoaded(long id, Uri imageUri, Drawable image) {
        if (id == R.id.static_map) {
            mStaticMap.setImageDrawable(image);
            mStaticMap.startAnimation(AnimationUtils.makeInAnimation(getActivity(), true));
            mStaticMap.setVisibility(View.VISIBLE);
        }
    }
}