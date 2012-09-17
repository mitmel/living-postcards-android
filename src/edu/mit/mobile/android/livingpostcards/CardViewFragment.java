package edu.mit.mobile.android.livingpostcards;

import android.app.Activity;
import android.database.Cursor;
import android.extracted.widget.AdapterViewFlipper;
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
import edu.mit.mobile.android.imagecache.ImageLoaderAdapter;
import edu.mit.mobile.android.imagecache.SimpleThumbnailCursorAdapter;
import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.livingpostcards.data.CardMedia;
import edu.mit.mobile.android.locast.net.NetworkClient;
import edu.mit.mobile.android.locast.sync.LocastSyncService;
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

    private Uri mCard;

    private AdapterViewFlipper mCardImage;

    private ImageView mStaticMap;

    private GoogleStaticMaps mStaticMapUtil;

    private int mTiming;

    private SimpleThumbnailCursorAdapter mAdapter;

    private Uri mCardMedia;

    private final static int LOADER_CARD = 100, LOADER_CARDMEDIA = 101;

    private static final String[] CARD_PROJECTION = new String[] { Card._ID,
            Card.COL_ANIMATED_RENDER, Card.COL_TITLE, Card.COL_DESCRIPTION, Card.COL_COVER_PHOTO,
            Card.COL_AUTHOR, Card.COL_TIMING, Card.COL_LATITUDE, Card.COL_LONGITUDE,
            Card.COL_MEDIA_URL };

    private static final String[] CARD_MEDIA_PROJECTION = new String[] { CardMedia._ID,
            CardMedia.COL_LOCAL_URL, CardMedia.COL_MEDIA_URL };

    private static final String[] CARD_MEDIA_FROM = new String[] { CardMedia.COL_LOCAL_URL,
            CardMedia.COL_MEDIA_URL };

    private static final int[] CARD_MEDIA_TO = new int[] { R.id.card_media_thumbnail,
            R.id.card_media_thumbnail };

    private static final int[] IMAGE_VIEW_IDS = new int[] { R.id.card_media_thumbnail };

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
            mCard = getArguments().getParcelable(ARGUMENT_URI);

            if (mCard != null) {
                mCardMedia = Card.MEDIA.getUri(mCard);
                getLoaderManager().initLoader(LOADER_CARD, null, this);
                getLoaderManager().initLoader(LOADER_CARDMEDIA, null, this);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.card_view_fragment, container, false);

        mCardImage = (AdapterViewFlipper) v.findViewById(R.id.card_image);
        mStaticMap = (ImageView) v.findViewById(R.id.static_map);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mAdapter = new SimpleThumbnailCursorAdapter(getActivity(), R.layout.card_media_fullsize,
                null, CARD_MEDIA_FROM, CARD_MEDIA_TO, IMAGE_VIEW_IDS, 0);

        mCardImage.setAdapter(new ImageLoaderAdapter(getActivity(), mAdapter, mImageCache,
                IMAGE_VIEW_IDS, 200, 200, ImageLoaderAdapter.UNIT_DIP));
    }

    @Override
    public void onPause() {
        super.onPause();

        mImageCache.unregisterOnImageLoadListener(this);
        mCardImage.stopFlipping();
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageCache.registerOnImageLoadListener(this);
        if (mTiming != 0) {
            mCardImage.startFlipping();
        }
        LocastSyncService.startExpeditedAutomaticSync(getActivity(), mCard);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        switch (id) {
            case LOADER_CARD:
                return new CursorLoader(getActivity(), mCard, CARD_PROJECTION, null, null, null);

            case LOADER_CARDMEDIA:
                return new CursorLoader(getActivity(), mCardMedia,
                        CARD_MEDIA_PROJECTION, null, null, null);
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
                    mCardImage.setFlipInterval(mTiming);
                    mCardImage.startFlipping();
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

            case LOADER_CARDMEDIA:
                mAdapter.setExpectedCount(c.getCount());
                if (BuildConfig.DEBUG) {
                    if (c.moveToFirst()) {
                        ProviderUtils.dumpCursorToLog(c, CARD_MEDIA_PROJECTION);
                    }
                }
                mAdapter.swapCursor(c);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_CARDMEDIA:
                mAdapter.swapCursor(null);
                break;
        }
    }

    @Override
    public void onImageLoaded(long id, Uri imageUri, Drawable image) {
        if (id == R.id.card_image) {

        } else if (id == R.id.static_map) {
            mStaticMap.setImageDrawable(image);
            mStaticMap.startAnimation(AnimationUtils.makeInAnimation(getActivity(), true));
            mStaticMap.setVisibility(View.VISIBLE);
        }
    }
}
