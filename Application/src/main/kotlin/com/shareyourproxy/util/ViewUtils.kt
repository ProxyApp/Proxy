package com.shareyourproxy.util

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.Resources
import android.graphics.*
import android.graphics.PorterDuff.Mode.SRC
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.Shader.TileMode.CLAMP
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.support.v4.content.ContextCompat.getColor
import android.support.v4.content.ContextCompat.getDrawable
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.facebook.drawee.drawable.ScalingUtils.ScaleType.CENTER_CROP
import com.facebook.drawee.drawable.ScalingUtils.ScaleType.FIT_CENTER
import com.facebook.drawee.generic.GenericDraweeHierarchy
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.generic.RoundingParams.asCircle
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.widget.ContentDescriptionDrawable
import timber.log.Timber

/**
 * Utility class for view functions.
 */
object ViewUtils {
    /**
     * Hide software keyboard.
     * @param view View to open keyboard for.
     */
    internal fun hideSoftwareKeyboard(view: View) {
        val manager = view.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Show software keyboard.
     * @param view View to open keyboard for.
     */
    internal fun showSoftwareKeyboard(view: View) {
        val manager = view.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        manager.showSoftInput(view, 0)
    }

    /**
     * Convert density pixels to pixels
     * @param res app resources
     * @param dp  dimension resource int. @BindDimen
     * @return float pixel size value
     */
    internal fun dpToPx(res: Resources, dp: Int): Int {
        return (dp / res.displayMetrics.density).toInt()
    }

    /**
     * Alpha a bitmap by drawing ARGB layer on top of it.
     * @param source Bitmap to alpha
     * @return the circular bitmap resource
     */
    internal fun getAlphaBitmapImage(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val bitmap = Bitmap.createBitmap(width, height, source.config)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(source, 0f, 0f, null)
        canvas.drawARGB(120, 0, 0, 0)
        source.recycle()
        return bitmap
    }

    /**
     * Paint a circular bitmap.
     * @param source          Bitmap to crop
     * @param backgroundColor of the bitmap
     * @return the circular bitmap resource
     */
    internal fun getCircularBitmapImage(source: Bitmap, backgroundColor: Int = Color.TRANSPARENT): Bitmap {
        val size = Math.min(source.width, source.height)

        val x = (source.width - size) / 2
        val y = (source.height - size) / 2

        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
        val config = if (source.config != null) source.config else Bitmap.Config.ARGB_8888

        if (squaredBitmap != source) {
            source.recycle()
        }

        val bitmap = Bitmap.createBitmap(size, size, config)
        val canvas = Canvas(bitmap)
        //Set a BitmapShader as the paint for the source bitmap to draw itself as a circle.
        val paint = Paint()
        val shader = BitmapShader(squaredBitmap, CLAMP, CLAMP)
        paint.setShader(shader)
        paint.isAntiAlias = true

        val r = size / 2f
        val rOffset = r - 2

        if (backgroundColor != Color.TRANSPARENT) {
            val backgroundPaint = Paint()
            backgroundPaint.color = backgroundColor
            backgroundPaint.isAntiAlias = true
            canvas.drawCircle(r, r, r, backgroundPaint)
            canvas.drawCircle(rOffset, rOffset, rOffset, paint)
        } else {
            canvas.drawCircle(r, r, r, paint)
        }
        squaredBitmap.recycle()
        return bitmap
    }

    /**
     * Paint a circular Drawable.
     * @param context         activity context
     * @param resourceId      Drawable to crop
     * @param backgroundColor of the bitmap
     * @return the circular bitmap resource
     */
    internal fun getCircularDrawableImage(
            context: Context, resourceId: Int, channelType: ChannelType, backgroundColor: Int): Drawable {
        val background = ShapeDrawable(OvalShape())
        background.setColorFilter(backgroundColor, SRC)

        val backgroundRadius = context.resources.getDimensionPixelSize(R.dimen.common_margin_xhuge)

        background.intrinsicWidth = backgroundRadius
        background.intrinsicHeight = backgroundRadius

        val source = svgToBitmapDrawable(context, resourceId,
                backgroundRadius, channelType.resColor)
        val layerDrawable = LayerDrawable(arrayOf(background, source))

        val inset = backgroundRadius / 4
        layerDrawable.setLayerInset(1, inset, inset, inset, inset)
        return layerDrawable
    }

    /**
     * Paint a circular bitmap.
     * @param context         activity context
     * @param source          drawable source
     * @param backgroundColor of the bitmap
     * @return the circular bitmap resource
     */
    internal fun getCircularDrawableImage(
            context: Context, source: Drawable, backgroundColor: Int): Drawable {
        val background = ShapeDrawable(OvalShape())
        background.setColorFilter(backgroundColor, SRC)

        val backgroundRadius = context.resources.getDimensionPixelSize(R.dimen.common_margin_large)
        background.intrinsicWidth = backgroundRadius
        background.intrinsicHeight = backgroundRadius

        val layerDrawable = LayerDrawable(arrayOf(background, source))
        val inset = backgroundRadius / 4
        layerDrawable.setLayerInset(1, inset, inset, inset, inset)
        return layerDrawable
    }

    /**
     * Use the DrawablCompat lib to tin a source image.drawable.
     * @param source image.drawable to tint
     * @param color  of tint
     * @return unwrapped tinted image.drawable
     */
    internal fun tintDrawableCompat(source: Drawable, color: Int): Drawable {
        val drawable = DrawableCompat.wrap(source)
        DrawableCompat.setTint(drawable, color)
        DrawableCompat.setTintMode(drawable, SRC_IN)
        return DrawableCompat.unwrap<Drawable>(drawable)
    }

    /**
     * Parse a SVG and return it as a [ContentDescriptionDrawable].
     * @param context    for resources
     * @param resourceId resource ID of the SVG
     * @param size       desired size of the icon
     * @return parsed image.drawable
     */
    internal fun svgToBitmapDrawable(context: Context, resourceId: Int, size: Int): Drawable {
        return svgToBitmapDrawable(context, resourceId, size, null)
    }

    /**
     * Parse a SVG and return it as a [ContentDescriptionDrawable].
     * @param context    for resources
     * @param resourceId resource ID of the SVG
     * @param size       desired size of the icon
     * @param color      desired color of the icon
     * @return parsed image.drawable
     */
    internal fun svgToBitmapDrawable(
            context: Context, resourceId: Int, size: Int, color: Int?): ContentDescriptionDrawable {
        val res = context.resources
        val dm = res.displayMetrics
        var drawable: Drawable? = null
        try {
            val svg = SVG.getFromResource(res, resourceId)
            val densitySize = dpToPx(res, size)

            val bmp = Bitmap.createBitmap(dm, densitySize, densitySize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            svg.renderToCanvas(canvas)

            drawable = DrawableCompat.wrap(ContentDescriptionDrawable(res, bmp))
            if (color != null) {
                DrawableCompat.setTint(drawable, color)
                DrawableCompat.setTintMode(drawable, SRC_IN)
            }
        } catch (e: SVGParseException) {
            Timber.e(Log.getStackTraceString(e))
        }

        return DrawableCompat.unwrap<ContentDescriptionDrawable>(drawable)
    }

    /**
     * Return a new Drawable of the entered resource icon.
     * @param resId icon resource id
     * @return menu icon image.drawable
     */
    internal fun getMenuIconDark(context: Context, resId: Int): ContentDescriptionDrawable {
        val res = context.resources
        val size = res.getDimensionPixelSize(R.dimen.common_svg_large)
        return svgToBitmapDrawable(context, resId, size,
                getColor(context, R.color.common_proxy_dark_selected))
    }

    /**
     * Return a new Drawable of the entered resource icon.
     * @param resId icon resource id
     * @return menu icon image.drawable
     */
    internal fun getMenuIcon(context: Context, resId: Int): ContentDescriptionDrawable {
        val res = context.resources
        val size = res.getDimensionPixelSize(R.dimen.common_svg_large)
        return svgToBitmapDrawable(context, resId, size,
                getColor(context, R.color.common_text_inverse))
    }

    /**
     * Return a new Drawable of the entered resource icon.
     * @param resId icon resource id
     * @return menu icon image.drawable
     */
    internal fun getMenuIconSecondary(context: Context, resId: Int): ContentDescriptionDrawable {
        val res = context.resources
        val size = res.getDimensionPixelSize(R.dimen.common_svg_large)
        return svgToBitmapDrawable(context, resId, size,
                getColor(context, R.color.common_text_secondary_inverse))
    }

    internal fun getUserImageHierarchyNoFade(context: Context): GenericDraweeHierarchy {
        val placeHolder = getDrawable(context, R.mipmap.ic_proxy)
        return GenericDraweeHierarchyBuilder(context.resources).setRoundingParams(asCircle()).setFailureImage(placeHolder, FIT_CENTER).setActualImageScaleType(FIT_CENTER).build()
    }

    internal fun getUserImageHierarchy(context: Context): GenericDraweeHierarchy {
        val placeHolder = getDrawable(context, R.mipmap.ic_proxy)
        return GenericDraweeHierarchyBuilder(context.resources).setFadeDuration(300).setRoundingParams(asCircle()).setPlaceholderImage(placeHolder, FIT_CENTER).setFailureImage(placeHolder, FIT_CENTER).setActualImageScaleType(FIT_CENTER).build()
    }

    internal fun getAlphaOverlayHierarchy(
            viewGroup: View, res: Resources): GenericDraweeHierarchy {
        val alphaDrawable = ShapeDrawable(RectShape())
        val lp = viewGroup.layoutParams
        alphaDrawable.intrinsicHeight = lp.height
        alphaDrawable.intrinsicWidth = lp.width
        alphaDrawable.paint.setARGB(40, 0, 0, 0)
        return GenericDraweeHierarchyBuilder(res).setOverlay(alphaDrawable).setBackground(ColorDrawable(Color.GRAY)).setActualImageScaleType(CENTER_CROP).build()
    }
}