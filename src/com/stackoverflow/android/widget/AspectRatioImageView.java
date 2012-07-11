package com.stackoverflow.android.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Preserves the aspect ratio of an image while allowing it to scale up.
 *
 * To use, set the width/height to 0dip of the side that you wish to resize. It will then be
 * adjusted based on the aspect ratio of the image.
 * 
 * {@link http
 * ://stackoverflow.com/questions/2991110/android-how-to-stretch-an-image-to-the-screen-width
 * -while-maintaining-aspect-ra/2999707}
 * 
 * 
 */
public class AspectRatioImageView extends ImageView {

	public AspectRatioImageView(Context context) {
		super(context);
	}

	public AspectRatioImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final Drawable drawable = getDrawable();
		boolean setMeasuredDimension = false;
		if (drawable != null) {

			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = MeasureSpec.getSize(heightMeasureSpec);

			if (MeasureSpec.EXACTLY == MeasureSpec.getMode(heightMeasureSpec) && height == 0) {


				final int diw = drawable.getIntrinsicWidth();
				if (diw > 0) {
					height = width * drawable.getIntrinsicHeight() / diw;
					setMeasuredDimension(width, height);
					setMeasuredDimension = true;
				}

			} else if (MeasureSpec.EXACTLY == MeasureSpec.getMode(widthMeasureSpec) && width == 0) {

				final int dih = drawable.getIntrinsicHeight();
				if (dih > 0) {
					width = height * drawable.getIntrinsicWidth() / dih;
					setMeasuredDimension(width, height);
					setMeasuredDimension = true;
				}
			}
		}

		if (!setMeasuredDimension) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
