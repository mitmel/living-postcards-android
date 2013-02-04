package edu.mit.mobile.android.widget;
/*
 * Copyright (C) 2012-2013  MIT Mobile Experience Lab
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import edu.mit.mobile.android.livingpostcards.R;

public class MultiLevelButton extends Button {

    private OnClickListener mWrappedOnClickListener;

    private OnChangeLevelListener mOnChangeLevelListener;

    private int mLevel;
    private int mMaxLevel;

    private final OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mWrappedOnClickListener != null) {
                mWrappedOnClickListener.onClick(v);
            }

            if (mOnChangeLevelListener != null) {
                final int curLevel = getLevel();

                final int newLevel = mOnChangeLevelListener.onChangeLevel(MultiLevelButton.this,
                        curLevel);
                if (newLevel != curLevel) {
                    setLevel(newLevel);
                }
            }
        }
    };

    public MultiLevelButton(Context context) {
        super(context);
        init(context, null);
    }

    public MultiLevelButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MultiLevelButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        super.setOnClickListener(mOnClickListener);
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MultiLevelButton);

        final int level = ta.getInteger(R.styleable.MultiLevelButton_level, 0);
        setLevel(level);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {

        mWrappedOnClickListener = l;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
        if (getBackground().setLevel(level)) {
            getBackground().invalidateSelf();
        }
    }

    public void setOnChangeLevelListener(OnChangeLevelListener l) {
        mOnChangeLevelListener = l;
    }

    public static interface OnChangeLevelListener {
        /**
         * @param b
         * @param curLevel
         * @return the desired level
         */
        public int onChangeLevel(MultiLevelButton b, int curLevel);
    }
}
