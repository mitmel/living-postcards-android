package edu.mit.mobile.android.livingpostcards;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageCache.OnImageLoadListener;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.livingpostcards.data.CardMedia;

public class CameraActivity extends FragmentActivity implements OnClickListener,
		OnImageLoadListener, OnCheckedChangeListener, LoaderCallbacks<Cursor> {

	private static final String TAG = CameraActivity.class.getSimpleName();

	public static final String ACTION_ADD_PHOTO = "edu.mit.mobile.android.ACTION_ADD_PHOTO";

	private Camera mCamera;
	private CameraPreview mPreview;

	private FrameLayout mPreviewHolder;

	private ImageCache mImageCache;

	private Uri mCard;

	private static final int LOADER_CARD = 100, LOADER_CARDMEDIA = 101;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_camera);

		// getActionBar().setDisplayHomeAsUpEnabled(true);

		mPreviewHolder = (FrameLayout) findViewById(R.id.camera_preview);

		findViewById(R.id.capture).setOnClickListener(this);
		((CompoundButton) findViewById(R.id.onion_skin_toggle)).setOnCheckedChangeListener(this);
		((CompoundButton) findViewById(R.id.grid_toggle)).setOnCheckedChangeListener(this);

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_camera, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showOnionskinImage(Uri image) {

		mImageCache.scheduleLoadImage(R.id.camera_preview, image, 640, 480);
	}

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
		mCamera.takePicture(null, null, mPictureCallback);
	}

	private final PictureCallback mPictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			savePicture(data);
		}
	};

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

	@SuppressWarnings("deprecation")
	@Override
	public void onImageLoaded(final long id, Uri imageUri, Drawable image) {
		if (R.id.camera_preview == id) {
			final ImageView iv = (ImageView) findViewById(R.id.onion_skin_image);
			iv.setImageDrawable(image);
			iv.setVisibility(View.VISIBLE);
			iv.setAlpha(80);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.onion_skin_toggle:
				findViewById(R.id.onion_skin_image).setVisibility(
						isChecked ? View.VISIBLE : View.GONE);

				break;

		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loader, Bundle args) {

		switch (loader) {
			case LOADER_CARD:
				return new CursorLoader(this, mCard, null, null, null,
						null);

			case LOADER_CARDMEDIA:
				return new CursorLoader(this, Card.MEDIA.getUri(mCard), null, null, null,
						null);

			default:
				return null;
		}

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		switch (loader.getId()) {
			case LOADER_CARD:
				if (c.moveToFirst()) {
					setTitle(c.getString(c.getColumnIndex(Card.NAME)));
				}
				break;

			case LOADER_CARDMEDIA:
				showLastPhoto(c);
				break;
		}
	}

	private void showLastPhoto(Cursor c) {
		if (c.moveToLast()) {
			final String localUrl = c.getString(c.getColumnIndex(CardMedia.MEDIA_LOCAL_URL));
			if (localUrl != null) {
				showOnionskinImage(Uri.parse(localUrl));
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {


	}

	private class SavePictureTask extends AsyncTask<byte[], Long, Uri> {
		private Exception mErr;

		@Override
		protected void onPreExecute() {
			CameraActivity.this.setProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}

		@Override
		protected Uri doInBackground(byte[]... data) {
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
			}
		}

		@Override
		protected void onPostExecute(Uri result) {
			if (mErr != null) {
				Log.e(TAG, "error writing file", mErr);
			}
			CameraActivity.this.setProgressBarIndeterminateVisibility(false);
		}
	}
}
