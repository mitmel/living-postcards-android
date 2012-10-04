package edu.mit.mobile.android.maps;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageCache.OnImageLoadListener;
import edu.mit.mobile.android.livingpostcards.BuildConfig;

public class GoogleStaticMapView extends ImageView {

    private static final String TAG = GoogleStaticMapView.class.getSimpleName();
    private ImageCache mImageCache;
    private GoogleStaticMaps mStaticMapUtil;
    private String mMarker = "size:mid|color:red";
    private boolean mSensor;
    private float mLongitude;
    private float mLatitude;

    private boolean mHasReceivedSet = false;

    private OnImageLoadListener mImageLoadListener;

    public GoogleStaticMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public GoogleStaticMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GoogleStaticMapView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mImageCache = ImageCache.getInstance(context);

        final Map<String, String> mapArgs = new HashMap<String, String>();

        mapArgs.put("zoom", "14");
        mapArgs.put("maptype", "terrain");

        mStaticMapUtil = new GoogleStaticMaps(context, mapArgs);
    }

    public void setMarker(String marker) {
        mMarker = marker;
    }

    public void setMap(float latitude, float longitude, boolean sensor) {
        mSensor = sensor;
        mLatitude = latitude;
        mLongitude = longitude;
        mHasReceivedSet = true;

        updateMap();
    }

    public void setOnImageLoadListener(OnImageLoadListener loadListener) {
        mImageLoadListener = loadListener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mImageCache.registerOnImageLoadListener(mInternalImageLoadListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mImageCache.cancel(getId());
        mImageCache.unregisterOnImageLoadListener(mInternalImageLoadListener);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateMap();
        }
    }

    private void updateMap() {
        if (!mHasReceivedSet) {
            return;
        }

        final int mapWidth = getWidth() - (getPaddingRight() + getPaddingLeft());
        final int mapHeight = getHeight() - (getPaddingTop() + getPaddingBottom());

        if (mapWidth <= 0 || mapHeight <= 0) {
            Log.e(TAG, "mapWidth or mapHeight were <=0. Not updating.");
            return;
        }

        final Uri staticMap = mStaticMapUtil.getMap(mLatitude, mLongitude, mapWidth, mapHeight,
                mSensor, mMarker);

        Log.d(TAG, "scheduling load of " + staticMap);
        try {
            final Drawable d = mImageCache.loadImage(getId(), staticMap, mapWidth, mapHeight);
            if (d != null) {
                mInternalImageLoadListener.onImageLoaded(getId(), staticMap, d);
            }
        } catch (final IOException e) {
            Log.e(TAG, "error updating static map", e);
        }
    }

    private final OnImageLoadListener mInternalImageLoadListener = new OnImageLoadListener() {
        @Override
        public void onImageLoaded(long id, Uri imageUri, Drawable image) {
            if (id == getId()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "loaded " + imageUri + " (id " + id + ")");
                }

                setImageDrawable(image);
                if (mImageLoadListener != null) {
                    mImageLoadListener.onImageLoaded(id, imageUri, image);
                }
            }
        }
    };
}
