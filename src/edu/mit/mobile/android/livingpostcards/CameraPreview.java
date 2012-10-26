package edu.mit.mobile.android.livingpostcards;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import edu.mit.mobile.android.locast.Constants;

/**
 * Camera preview, based on the Android example code.
 *
 * If the width isn't specified, will resize to keep the aspect ratio of the preview image. Sets the
 * preview size to the optimal size based on this view.
 *
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraPreview.class.getSimpleName();
    private final SurfaceHolder mHolder;
    private final Camera mCamera;
    private float mForceAspectRatio = 0f;
    private OnPreviewStartedListener mOnPreviewStartedListener;

    private static final float ASPECT_RATIO_TOLERENCE = 0.001f;

    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, final Camera camera) {
        super(context);
        mCamera = camera;

        if (mCamera == null) {
            throw new NullPointerException("camera cannot be null");
        }

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Instead of letting the preview pick the best size based on
     *
     * @param widthByHeight
     */
    public void setForceAspectRatio(float widthByHeight) {
        mForceAspectRatio = widthByHeight;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (final IOException e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        } catch (final RuntimeException e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (final Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        final Parameters p = mCamera.getParameters();

        Camera.Size size = getBestPreviewSize(w, h, p, mForceAspectRatio);

        // This means our screen is probably small and there are no pixel-for-pixel preview sizes
        // that have the same aspect ratio. Try searching from the original picture size and letting
        // the UI scale.
        if (size == null) {
            Log.w(TAG,
                    "Couldn't find the best size that maps pixel-for-pixel on the screen. Trying larger ones...");
            final Size picSize = p.getPictureSize();
            size = getBestPreviewSize(picSize.width, picSize.height, p, (float) picSize.width
                    / picSize.height);
        }

        // ok, this isn't good. But it's better than failing.
        if (size == null) {
            Log.e(TAG,
                    "Trying to find the best size, but couldn't find one with the request aspect ratio ("
                            + mForceAspectRatio + "). Ignoring ratio request...");
            size = getBestPreviewSize(w, h, p, 0f);
        }

        p.setPreviewSize(size.width, size.height);

        mCamera.setParameters(p);

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            if (mOnPreviewStartedListener != null) {
                mOnPreviewStartedListener.onPreviewStarted();
            }

        } catch (final Exception e) {
            Log.e(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int wMode = View.MeasureSpec.getMode(widthMeasureSpec);
        final int hMode = View.MeasureSpec.getMode(heightMeasureSpec);
        // if the size is specified exactly, we won't attempt to resize ourselves
        if (wMode == View.MeasureSpec.EXACTLY && hMode == View.MeasureSpec.EXACTLY) {
            if (Constants.DEBUG) {
                Log.d(TAG, "not correcting aspect ratio for preview");
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int w = View.MeasureSpec.getSize(widthMeasureSpec);
        int h = View.MeasureSpec.getSize(heightMeasureSpec);

        final Parameters p = mCamera.getParameters();

        // the aspect ratio of the preview is often wrong. We need to use the aspect ratio of the
        // picture instead.
        final Camera.Size picSize = p.getPictureSize();

        if (wMode == View.MeasureSpec.AT_MOST) {
            // preserve aspect ratio
            w = (int) (((float) picSize.width / picSize.height) * h);
        } else if (hMode == View.MeasureSpec.AT_MOST) {
            // preserve aspect ratio
            h = (int) (((float) picSize.height / picSize.width) * w);
        } else {
            if (Constants.DEBUG) {
                Log.d(TAG, "not correcting aspect ratio for preview");
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        if (Constants.DEBUG) {
            Log.d(TAG, "Adjusting view to match aspect ratio of preview " + picSize.width + "x"
                    + picSize.height + "; set to " + w + "x" + h);
        }
        setMeasuredDimension(w, h);
    }

    /***
     * Copyright (c) 2008-2012 CommonsWare, LLC Licensed under the Apache License, Version 2.0 (the
     * "License"); you may not use this file except in compliance with the License. You may obtain a
     * copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by
     * applicable law or agreed to in writing, software distributed under the License is distributed
     * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and limitations under the
     * License. From _The Busy Coder's Guide to Advanced Android Development_
     * http://commonsware.com/AdvAndroid
     */

    /**
     * Finds the highest resolution preview size that fits within the given width and height.
     *
     * @param width
     * @param height
     * @param parameters
     * @return
     */
    public static Camera.Size getBestPreviewSize(int width, int height,
            Camera.Parameters parameters, float widthByHeight) {
        Camera.Size result = null;


        if (Constants.DEBUG) {
            Log.d(TAG, "Looking for best preview size that fits within " + width + "x" + height
                    + (widthByHeight != 0f ? " and has aspect ratio of " + widthByHeight : ""));
            final StringBuilder sb = new StringBuilder();
            for (final Camera.Size size : parameters.getSupportedPreviewSizes()) {
                sb.append(size.width);
                sb.append('x');
                sb.append(size.height);
                sb.append(" ");
            }
            Log.d(TAG, "available sizes: " + sb.toString());
        }
        for (final Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (widthByHeight != 0f
                    && Math.abs((float) size.width / size.height - widthByHeight) >= ASPECT_RATIO_TOLERENCE) {
                continue;
            }
            if (Constants.DEBUG && widthByHeight != 0f) {
                Log.d(TAG, size.width + "x" + size.height + " has the correct aspect ratio");
            }

            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    final int resultArea = result.width * result.height;
                    final int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        if (Constants.DEBUG) {
            if (result == null) {
                Log.e(TAG, "Couldn't find a size that matches the requirements.");
            } else {
                Log.d(TAG, "choose " + result.width + "x" + result.height);
            }
        }
        return result;
    }

    public void setOnPreviewStartedListener(OnPreviewStartedListener l) {
        mOnPreviewStartedListener = l;
    }

    public interface OnPreviewStartedListener {
        public void onPreviewStarted();
    }
}