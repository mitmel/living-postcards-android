package edu.mit.mobile.android.livingpostcards;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraActivity extends Activity implements OnClickListener {

	private static final String TAG = CameraActivity.class.getSimpleName();

	private Camera mCamera;
	private CameraPreview mPreview;

	private FrameLayout mPreviewHolder;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
		// getActionBar().setDisplayHomeAsUpEnabled(true);

		mPreviewHolder = (FrameLayout) findViewById(R.id.camera_preview);

		findViewById(R.id.capture).setOnClickListener(this);

		final Intent intent = getIntent();
		final String action = intent.getAction();

		if (Intent.ACTION_INSERT.equals(action)) {
			showOnionskinImage(intent.getData());
		}
    }

	@Override
	protected void onPause() {
		super.onPause();

		mCamera.stopPreview();

		releaseCamera();
	}

	@Override
	protected void onResume() {
		super.onResume();

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
		final ImageView iv = (ImageView) findViewById(R.id.onion_skin_image);
		iv.setImageURI(image);
		iv.setAlpha(80);


	}

	private void adjustPreviewToOnionSkin() {
		final ImageView iv = (ImageView) findViewById(R.id.onion_skin_image);
		final LayoutParams lp = mPreviewHolder.getLayoutParams();

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
        }
        catch (final Exception e){
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


			StorageUtils.EXTERNAL_PICTURES_DIR.mkdirs();

			final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			final File outFile = new File(StorageUtils.EXTERNAL_PICTURES_DIR, "IMG_" + timeStamp + ".jpg");

			try {
				final FileOutputStream fos = new FileOutputStream(outFile);
				fos.write(data);
				fos.close();
			} catch (final IOException e) {
				Log.e(TAG, "error writing file", e);
			}
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.capture:
				capture();
				break;
		}
	}
}
