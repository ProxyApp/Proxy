package com.shareyourproxy.widget

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable

/**
 * This BitmapDrawable has a name. For now this functionality is being used to display the [ContentDescriptionDrawable.contentDescription] in a Toast
 * when a user long presses a tab [android.support.design.widget.TabLayout].
 * Create an image from a bitmap, setting initial target density based on the display metrics of the resources.
 * @param res display metrics
 * @param bmp Bitmap
 */
internal final class ContentDescriptionDrawable(res: Resources, bmp: Bitmap) : BitmapDrawable(res, bmp) {
    internal var contentDescription = ""
    internal fun setContentDescription(string: String): ContentDescriptionDrawable {
        contentDescription = string
        return this
    }
}
