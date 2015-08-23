package com.shareyourproxy.widget;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

/**
 * This BitmapDrawable has a name. For now this functionality is being used to display the {@link
 * ContentDescriptionDrawable#_contentDescription} in a Toast when a user long presses a tab {@link
 * android.support.design.widget.TabLayout}.
 */
public class ContentDescriptionDrawable extends BitmapDrawable {

    private String _contentDescription = "";

    /**
     * Create image.drawable from a bitmap, setting initial target density based on the display
     * metrics of the resources.
     *
     * @param res display metrics
     * @param bmp Bitmap
     */
    public ContentDescriptionDrawable(Resources res, Bitmap bmp) {
        super(res, bmp);
    }

    /**
     * Getter.
     *
     * @return content description
     */
    public String getContentDescription() {
        return _contentDescription;
    }

    /**
     * Setter.
     *
     * @param contentDescription description of image.drawable
     * @return this
     */
    public ContentDescriptionDrawable setContentDescription(String contentDescription) {
        this._contentDescription = contentDescription;
        return this;
    }

}
