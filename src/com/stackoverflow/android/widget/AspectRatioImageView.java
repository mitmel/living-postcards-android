package com.stackoverflow.android.widget;

import java.lang.reflect.Field;

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

    int mMaxWidth = Integer.MAX_VALUE;
    int mMaxHeight = Integer.MAX_VALUE;

    public AspectRatioImageView(Context context) {
        super(context);
        init(context, null);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // http://stackoverflow.com/questions/8311081/how-to-get-maxwidth-and-maxheight-parameters-of-imageview
        // haaaaack
        try {
            final Field maxWidthField = ImageView.class.getDeclaredField("mMaxWidth");
            final Field maxHeightField = ImageView.class.getDeclaredField("mMaxHeight");
            maxWidthField.setAccessible(true);
            maxHeightField.setAccessible(true);

            mMaxWidth = (Integer) maxWidthField.get(this);
            mMaxHeight = (Integer) maxHeightField.get(this);
        } catch (final SecurityException e) {
            // we don't care if we can't get it. We weren't really supposed to anyhow.
        } catch (final NoSuchFieldException e) {
            // we don't care if we can't get it. We weren't really supposed to anyhow.
        } catch (final IllegalArgumentException e) {
            // we don't care if we can't get it. We weren't really supposed to anyhow.
        } catch (final IllegalAccessException e) {
            // we don't care if we can't get it. We weren't really supposed to anyhow.
        }
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
                    height = Math.min(width * drawable.getIntrinsicHeight() / diw, mMaxHeight);
                    setMeasuredDimension(width, height);
                    setMeasuredDimension = true;
                }

            } else if (MeasureSpec.EXACTLY == MeasureSpec.getMode(widthMeasureSpec) && width == 0) {

                final int dih = drawable.getIntrinsicHeight();
                if (dih > 0) {
                    width = Math.min(height * drawable.getIntrinsicWidth() / dih, mMaxWidth);
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
