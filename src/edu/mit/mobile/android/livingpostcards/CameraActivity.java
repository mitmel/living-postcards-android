package edu.mit.mobile.android.livingpostcards;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

public class CameraActivity extends Activity {

	private static final String TAG = CameraActivity.class.getSimpleName();

	private Camera mCamera;
	private CameraPreview mPreview;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
		// getActionBar().setDisplayHomeAsUpEnabled(true);

		mCamera = getCameraInstance();

		mPreview = new CameraPreview(this, mCamera);

		((FrameLayout) findViewById(R.id.camera_preview)).addView(mPreview);
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

}
