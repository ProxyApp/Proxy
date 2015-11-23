package com.shareyourproxy.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.widget.ContentDescriptionDrawable;

import timber.log.Timber;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.graphics.PorterDuff.Mode.SRC;
import static android.graphics.PorterDuff.Mode.SRC_IN;
import static android.support.v4.content.ContextCompat.getColor;
import static android.support.v4.content.ContextCompat.getDrawable;
import static com.facebook.drawee.drawable.ScalingUtils.ScaleType.CENTER_CROP;
import static com.facebook.drawee.drawable.ScalingUtils.ScaleType.FIT_CENTER;
import static com.facebook.drawee.generic.RoundingParams.asCircle;

/**
 * Utility class for view functions.
 */
public class ViewUtils {

    /**
     * Private Constructor
     */
    private ViewUtils() {
    }

    /**
     * Hide software keyboard.
     *
     * @param view View to open keyboard for.
     */
    public static void hideSoftwareKeyboard(@NonNull final View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext()
            .getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Show software keyboard.
     *
     * @param view View to open keyboard for.
     */
    public static void showSoftwareKeyboard(@NonNull final View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext()
            .getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            manager.showSoftInput(view, 0);
        }
    }

    /**
     * Convert density pixels to pixels
     *
     * @param res app resources
     * @param dp  dimension resource int. @BindDimen
     * @return float pixel size value
     */
    public static int dpToPx(Resources res, int dp) {
        return (int) (dp / res.getDisplayMetrics().density);
    }

    /**
     * Alpha a bitmap by drawing ARGB layer on top of it.
     *
     * @param source Bitmap to alpha
     * @return the circular bitmap resource
     */
    public static Bitmap getAlphaBitmapImage(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(
            width, height, source.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(source, 0, 0, null);
        canvas.drawARGB(120, 0, 0, 0);
        source.recycle();
        return bitmap;
    }

    /**
     * Paint a circular bitmap.
     *
     * @param source Bitmap to crop
     * @return the circular bitmap resource
     */
    public static Bitmap getCircularBitmapImage(Bitmap source) {
        return getCircularBitmapImage(source, Color.TRANSPARENT);
    }

    /**
     * Paint a circular bitmap.
     *
     * @param source          Bitmap to crop
     * @param backgroundColor of the bitmap
     * @return the circular bitmap resource
     */
    public static Bitmap getCircularBitmapImage(Bitmap source, int backgroundColor) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap.Config config = source.getConfig() != null ?
            source.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(size, size, config);

        Canvas canvas = new Canvas(bitmap);
        //Set a BitmapShader as the paint for the source bitmap to draw itself as a circle.
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP,
            BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        float rOffset = r - 2;

        if (backgroundColor != Color.TRANSPARENT) {
            Paint backgroundPaint = new Paint();
            backgroundPaint.setColor(backgroundColor);
            backgroundPaint.setAntiAlias(true);
            canvas.drawCircle(r, r, r, backgroundPaint);
            canvas.drawCircle(rOffset, rOffset, rOffset, paint);
        } else {
            canvas.drawCircle(r, r, r, paint);
        }
        squaredBitmap.recycle();
        return bitmap;
    }

    /**
     * Paint a circular Drawable.
     *
     * @param context         activity context
     * @param resourceId      Drawable to crop
     * @param backgroundColor of the bitmap
     * @return the circular bitmap resource
     */
    public static Drawable getCircularDrawableImage(
        Context context, int resourceId, ChannelType channelType, int backgroundColor) {
        ShapeDrawable background = new ShapeDrawable(new OvalShape());
        background.setColorFilter(backgroundColor, SRC);

        int backgroundRadius = context.getResources()
            .getDimensionPixelSize(R.dimen.common_margin_xhuge);

        background.setIntrinsicWidth(backgroundRadius);
        background.setIntrinsicHeight(backgroundRadius);

        BitmapDrawable source = svgToBitmapDrawable(context, resourceId,
            backgroundRadius, channelType.getResColor());
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{ background, source });

        int inset = backgroundRadius / 4;
        layerDrawable.setLayerInset(1, inset, inset, inset, inset);
        return layerDrawable;
    }

    /**
     * Paint a circular bitmap.
     *
     * @param context         activity context
     * @param source          drawable source
     * @param backgroundColor of the bitmap
     * @return the circular bitmap resource
     */
    public static Drawable getCircularDrawableImage(
        Context context, Drawable source, int backgroundColor) {
        ShapeDrawable background = new ShapeDrawable(new OvalShape());
        background.setColorFilter(backgroundColor, SRC);

        int backgroundRadius = context.getResources()
            .getDimensionPixelSize(R.dimen.common_margin_large);
        background.setIntrinsicWidth(backgroundRadius);
        background.setIntrinsicHeight(backgroundRadius);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{ background, source });
        int inset = backgroundRadius / 4;
        layerDrawable.setLayerInset(1, inset, inset, inset, inset);
        return layerDrawable;
    }

    /**
     * Use the DrawablCompat lib to tin a source image.drawable.
     *
     * @param source image.drawable to tint
     * @param color  of tint
     * @return unwrapped tinted image.drawable
     */
    public static Drawable tintDrawableCompat(Drawable source, int color) {
        Drawable drawable = DrawableCompat.wrap(source);
        DrawableCompat.setTint(drawable, color);
        DrawableCompat.setTintMode(drawable, SRC_IN);
        return DrawableCompat.unwrap(drawable);
    }

    /**
     * Parse a SVG and return it as a {@link ContentDescriptionDrawable}.
     *
     * @param context    for resources
     * @param resourceId resource ID of the SVG
     * @param size       desired size of the icon
     * @return parsed image.drawable
     */
    public static Drawable svgToBitmapDrawable(Context context, int resourceId, int size) {
        return svgToBitmapDrawable(context, resourceId, size, null);
    }

    /**
     * Parse a SVG and return it as a {@link ContentDescriptionDrawable}.
     *
     * @param context    for resources
     * @param resourceId resource ID of the SVG
     * @param size       desired size of the icon
     * @param color      desired color of the icon
     * @return parsed image.drawable
     */
    public static ContentDescriptionDrawable svgToBitmapDrawable(
        Context context, int resourceId, int size, Integer color) {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Drawable drawable = null;
        try {
            SVG svg = SVG.getFromResource(res, resourceId);
            int densitySize = dpToPx(res, size);

            Bitmap bmp = Bitmap.createBitmap(dm, densitySize, densitySize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            svg.renderToCanvas(canvas);

            drawable = DrawableCompat.wrap(new ContentDescriptionDrawable(res, bmp));
            if (color != null) {
                DrawableCompat.setTint(drawable, color);
                DrawableCompat.setTintMode(drawable, SRC_IN);
            }
        } catch (SVGParseException e) {
            Timber.e(Log.getStackTraceString(e));
        }
        return DrawableCompat.unwrap(drawable);
    }

    /**
     * Return a new Drawable of the entered resource icon.
     *
     * @param resId icon resource id
     * @return menu icon image.drawable
     */
    public static ContentDescriptionDrawable getMenuIconDark(Context context, int resId) {
        Resources res = context.getResources();
        int size = res.getDimensionPixelSize(R.dimen.common_svg_large);
        return svgToBitmapDrawable(context, resId, size,
            getColor(context, R.color.common_proxy_dark_selected));
    }

    /**
     * Return a new Drawable of the entered resource icon.
     *
     * @param resId icon resource id
     * @return menu icon image.drawable
     */
    public static ContentDescriptionDrawable getMenuIcon(Context context, int resId) {
        Resources res = context.getResources();
        int size = res.getDimensionPixelSize(R.dimen.common_svg_large);
        return svgToBitmapDrawable(context, resId, size,
            getColor(context, R.color.common_text_inverse));
    }

    /**
     * Return a new Drawable of the entered resource icon.
     *
     * @param resId icon resource id
     * @return menu icon image.drawable
     */
    public static ContentDescriptionDrawable getMenuIconSecondary(Context context, int resId) {
        Resources res = context.getResources();
        int size = res.getDimensionPixelSize(R.dimen.common_svg_large);
        return svgToBitmapDrawable(context, resId, size,
            getColor(context, R.color.common_text_secondary_inverse));
    }

    public static GenericDraweeHierarchy getUserImageHierarchyNoFade(Context context) {
        Drawable placeHolder = getDrawable(context, R.mipmap.ic_proxy);
        return new GenericDraweeHierarchyBuilder(context.getResources())
            .setRoundingParams(asCircle())
            .setActualImageScaleType(FIT_CENTER)
            .build();
    }

    public static GenericDraweeHierarchy getUserImageHierarchy(Context context) {
        Drawable placeHolder = getDrawable(context, R.mipmap.ic_proxy);
        return new GenericDraweeHierarchyBuilder(context.getResources())
            .setFadeDuration(300)
            .setRoundingParams(asCircle())
            .setPlaceholderImage(placeHolder, FIT_CENTER)
            .setActualImageScaleType(FIT_CENTER)
            .build();
    }

    public static GenericDraweeHierarchy getAlphaOverlayHierarchy(
        View viewGroup, Resources res) {
        ShapeDrawable alphaDrawable = new ShapeDrawable(new RectShape());
        ViewGroup.LayoutParams lp = viewGroup.getLayoutParams();
        alphaDrawable.setIntrinsicHeight(lp.height);
        alphaDrawable.setIntrinsicWidth(lp.width);
        alphaDrawable.getPaint().setARGB(40, 0, 0, 0);
        return new GenericDraweeHierarchyBuilder(res)
            .setOverlay(alphaDrawable)
            .setBackground(new ColorDrawable(Color.GRAY))
            .setActualImageScaleType(CENTER_CROP)
            .build();
    }

}
