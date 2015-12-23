package com.shareyourproxy.widget

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable

/**
 * This BitmapDrawable has a name. For now this functionality is being used to display the [ContentDescriptionDrawable._contentDescription] in a Toast
 * when a user long presses a tab [android.support.design.widget.TabLayout].
 * Create an image from a bitmap, setting initial target density based on the display metrics of the resources.
 * @param res display metrics
 * @param bmp Bitmap
 */
class ContentDescriptionDrawable(res: Resources, bmp: Bitmap) : BitmapDrawable(res, bmp) {
    private var _contentDescription = ""

    /**
     * Getter.
     * @return content description
     */
    fun getContentDescription(): String {
        return _contentDescription
    }

    /**
     * Setter.
     * @param contentDescription description of image.drawable
     * @return this
     */
    fun setContentDescription(contentDescription: String): ContentDescriptionDrawable {
        this._contentDescription = contentDescription
        return this
    }

}
