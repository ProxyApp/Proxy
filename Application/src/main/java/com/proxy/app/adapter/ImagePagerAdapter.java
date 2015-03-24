package com.proxy.app.adapter;

import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Simple abstraction for {@link com.proxy.widget.SlidingTabLayout} to display a Drawable rather
 * than text.
 */
public abstract class ImagePagerAdapter extends FragmentPagerAdapter {

    /**
     * Constructor.
     *
     * @param fragmentManager Manager of Fragements
     */
    public ImagePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    /**
     * Get the Drawable paired with its coresponding fragment by position.
     *
     * @param position position of drawable in list.
     * @return Drawable icon
     */
    public abstract Drawable getPageImage(int position);

    /**
     * Get the description of the Drawable.
     *
     * @param position position of the drawable in list.
     * @return content description
     */
    public abstract String getImageDescription(int position);
}
