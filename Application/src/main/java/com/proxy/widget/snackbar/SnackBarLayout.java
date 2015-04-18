/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 William Mora
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.proxy.widget.snackbar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Custom Linear Layout with maximum width and height functions to control layout measurement.
 */
public class SnackBarLayout extends LinearLayout {
    private int mMaxWidth = Integer.MAX_VALUE;
    private int mMaxHeight = Integer.MAX_VALUE;

    /**
     * Constructor.
     * @param context context
     */
    public SnackBarLayout(Context context) {
        super(context);
    }

    /**
     * Constructor. 
     * @param context context
     * @param attrs attrs
     */
    public SnackBarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor. 
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public SnackBarLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Adjust width as necessary
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (mMaxWidth < width) {
            int mode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, mode);
        }
        // Adjust height as necessary
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mMaxHeight < height) {
            int mode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, mode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * set maximum width per android design guidelines.
     * @param maxWidth width in density pixels
     */
    @SuppressWarnings("unused")
    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
        requestLayout();
    }

    /**
     * Set the Maximum height for the snack view. 48dp for 1 line tablet, 80dp for 2 line phones.
     * @param maxHeight height in density pixels
     */
    @SuppressWarnings("unused")
    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
        requestLayout();
    }

}
