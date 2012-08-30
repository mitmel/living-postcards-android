package edu.mit.mobile.android.livingpostcards;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.ActionBarSherlock.OnCreateOptionsMenuListener;
import com.actionbarsherlock.ActionBarSherlock.OnOptionsItemSelectedListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageCache.OnImageLoadListener;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.livingpostcards.data.CardMedia;

public class CameraActivity extends FragmentActivity implements OnClickListener,
        OnImageLoadListener, OnCheckedChangeListener, LoaderCallbacks<Cursor>,
        OnOptionsItemSelectedListener, OnCreateOptionsMenuListener {

    private static final String TAG = CameraActivity.class.getSimpleName();

    public static final String ACTION_ADD_PHOTO = "edu.mit.mobile.android.ACTION_ADD_PHOTO";

    private final ActionBarSherlock mSherlock = ActionBarSherlock.wrap(this);

    private Camera mCamera;
    private CameraPreview mPreview;

    private FrameLayout mPreviewHolder;

    private ImageCache mImageCache;

    private Uri mCard;

    private ImageView mOnionSkin;

    private Button mCaptureButton;

    private CompoundButton mOnionskinToggle;

    private static final int LOADER_CARD = 100, LOADER_CARDMEDIA = 101;

    private static final String[] CARD_MEDIA_PROJECTION = new String[] { CardMedia._ID,
            CardMedia.MEDIA_LOCAL_URL };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        mSherlock.setContentView(R.layout.activity_camera);

        // getActionBar().setDisplayHomeAsUpEnabled(true);

        mPreviewHolder = (FrameLayout) findViewById(R.id.camera_preview);

        mOnionSkin = (ImageView) findViewById(R.id.onion_skin_image);

        mCaptureButton = (Button) findViewById(R.id.capture);
        mCaptureButton.setOnClickListener(this);

        mOnionskinToggle = (CompoundButton) findViewById(R.id.onion_skin_toggle);
        mOnionskinToggle.setOnCheckedChangeListener(this);
        ((CompoundButton) findViewById(R.id.grid_toggle)).setOnCheckedChangeListener(this);

        setFullscreen(true);

        mImageCache = ImageCache.getInstance(this);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (ACTION_ADD_PHOTO.equals(action)) {
            mCard = intent.getData();

            getSupportLoaderManager().initLoader(LOADER_CARD, null, this);

            getSupportLoaderManager().initLoader(LOADER_CARDMEDIA, null, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mImageCache.unregisterOnImageLoadListener(this);

        mCamera.stopPreview();

        releaseCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mImageCache.registerOnImageLoadListener(this);

        mCamera = getCameraInstance();

        if (mCamera != null) {
            mPreview = new CameraPreview(this, mCamera);

            mPreviewHolder.addView(mPreview);

        } else {
            Toast.makeText(this, "Error initializing camera", Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        setOnionSkinVisible(mOnionskinToggle.isChecked());
    }

    public void setFullscreen(boolean fullscreen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                final Method setSystemUiVisibility = View.class.getMethod("setSystemUiVisibility",
                        int.class);
                setSystemUiVisibility.invoke(mPreviewHolder,
                        fullscreen ? View.SYSTEM_UI_FLAG_LOW_PROFILE : 0);

            } catch (final NoSuchMethodException e) {
                Log.e(TAG, "missing setSystemUiVisibility method, despite version checking", e);
            } catch (final IllegalArgumentException e) {
                Log.e(TAG, "reflection error", e);
            } catch (final IllegalAccessException e) {
                Log.e(TAG, "reflection error", e);
            } catch (final InvocationTargetException e) {
                Log.e(TAG, "reflection error", e);
            }
        }

        if (fullscreen) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mSherlock.getMenuInflater().inflate(R.menu.activity_camera, menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        return mSherlock.dispatchCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.done:
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        return mSherlock.dispatchOptionsItemSelected(item);
    }

    /**
     * Loads the given image in the onion skin. This requests the image cache to load it, so it
     * returns immediately.
     *
     * @param image
     */
    private void showOnionskinImage(Uri image) {
        try {
            final Drawable d = mImageCache.loadImage(R.id.camera_preview, image, 640, 480);
            if (d != null) {
                loadOnionskinImage(d);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void invalidateOnionskinImage() {
        mOnionSkin.setImageDrawable(null);
        mOnionskinToggle.setEnabled(false);
    }

    private void loadOnionskinImage(Drawable image) {
        mOnionSkin.setImageDrawable(image);
        mOnionSkin.setAlpha(80);
        mOnionskinToggle.setEnabled(true);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onImageLoaded(final long id, Uri imageUri, Drawable image) {
        if (R.id.camera_preview == id) {

            loadOnionskinImage(image);
        }
    }

    // ////////////////////////////////////////////////////////////
    // /// camera
    // ///////////////////////////////////////////////////////////

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (final Exception e) {
            Log.e(TAG, "Error acquiring camera", e);
        }
        return c; // returns null if camera is unavailable
    }

    private void capture() {
        invalidateOnionskinImage();
        mCaptureButton.setEnabled(false);
        mCamera.takePicture(null, null, mPictureCallback);
    }

    private final PictureCallback mPictureCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            setFullscreen(true);
            savePicture(data);
            mCamera.startPreview();
        }
    };

    /**
     * Saves the picture as a jpeg to disk and adds it as a media item. This method starts a task
     * and returns immediately.
     *
     * @param data
     */
    private void savePicture(byte[] data) {
        new SavePictureTask().execute(data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.capture:
                capture();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.onion_skin_toggle:
                setOnionSkinVisible(isChecked);

                break;

            case R.id.grid_toggle:
                findViewById(R.id.grid).setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }
    }

    private void setOnionSkinVisible(boolean isChecked) {

        mOnionSkin.setVisibility(isChecked ? View.VISIBLE : View.GONE);
    }

    // /////////////////////////////////////////////////////////////////////
    // content loading
    // /////////////////////////////////////////////////////////////////////

    @Override
    public Loader<Cursor> onCreateLoader(int loader, Bundle args) {

        switch (loader) {
            case LOADER_CARD:
                return new CursorLoader(this, mCard, null, null, null, null);

            case LOADER_CARDMEDIA:
                return new CursorLoader(this, Card.MEDIA.getUri(mCard), CARD_MEDIA_PROJECTION,
                        null, null,
                        CardMedia._ID + " DESC LIMIT 1");

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        switch (loader.getId()) {
            case LOADER_CARD:
                if (c.moveToFirst()) {
                    mSherlock.setTitle(c.getString(c.getColumnIndex(Card.NAME)));
                }
                break;

            case LOADER_CARDMEDIA:
                showLastPhoto(c);
                break;
        }
    }

    /**
     * Given a card media cursor, load the most recent photo. This assumes that the cursor was
     * queried such that the most recent item is last in the cursor (the default sort does this).
     *
     * @param cardMedia
     */
    private void showLastPhoto(Cursor cardMedia) {
        if (cardMedia.moveToLast()) {
            final String localUrl = cardMedia.getString(cardMedia
                    .getColumnIndex(CardMedia.MEDIA_LOCAL_URL));
            if (localUrl != null) {
                showOnionskinImage(Uri.parse(localUrl));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        invalidateOnionskinImage();
    }

    /**
     * Saves the given jpeg bytes to disk and adds an entry to the CardMedia list. Pictures are
     * stored to external storage under {@link StorageUtils#EXTERNAL_PICTURE_SUBDIR}.
     *
     */
    private class SavePictureTask extends AsyncTask<byte[], Long, Uri> {
        private Exception mErr;

        @Override
        protected void onPreExecute() {
            CameraActivity.this.setProgressBarIndeterminateVisibility(true);
            super.onPreExecute();
        }

        @Override
        protected Uri doInBackground(byte[]... data) {
            if (data == null || data.length == 0 || data[0].length == 0) {
                mErr = new IllegalArgumentException("data was null or empty");
                return null;
            }
            final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            final File outFile = new File(StorageUtils.EXTERNAL_PICTURES_DIR, "IMG_" + timeStamp
                    + ".jpg");

            StorageUtils.EXTERNAL_PICTURES_DIR.mkdirs();

            try {
                final FileOutputStream fos = new FileOutputStream(outFile);
                fos.write(data[0]);
                fos.close();

                mImageCache.scheduleLoadImage(0, Uri.fromFile(outFile), 640, 480);

                final ContentValues cv = new ContentValues();

                cv.put(CardMedia.MEDIA_LOCAL_URL, Uri.fromFile(outFile).toString());
                cv.put(CardMedia.UUID, UUID.randomUUID().toString());

                return Card.MEDIA.insert(getContentResolver(), mCard, cv);

            } catch (final IOException e) {
                mErr = e;
                return null;
            } catch (final RuntimeException re) {
                mErr = re;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Uri result) {
            if (mErr != null) {
                Log.e(TAG, "error writing file", mErr);
                Toast.makeText(CameraActivity.this,
                        "Sorry, there was an error while saving the photo. Please try again.",
                        Toast.LENGTH_LONG).show();
            }
            CameraActivity.this.setProgressBarIndeterminateVisibility(false);
            mCaptureButton.setEnabled(true);
        }
    }
}
