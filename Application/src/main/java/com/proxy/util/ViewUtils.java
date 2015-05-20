package com.proxy.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.proxy.R;
import com.proxy.widget.ContentDescriptionDrawable;

import timber.log.Timber;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.pm.PackageManager.NameNotFoundException;
import static android.graphics.PorterDuff.Mode.SRC;
import static android.graphics.PorterDuff.Mode.SRC_IN;
import static com.proxy.util.DebugUtils.getSimpleName;

/**
 * Utility class for view functions.
 */
@SuppressWarnings("unused")
public class ViewUtils {

    public static final String TAG = getSimpleName(ViewUtils.class);
    public static final int NO_COLOR = Integer.MIN_VALUE;

    /**
     * Constructor
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
     * @param res   app resources
     * @param resId dimension resource
     * @return float pixel size value
     */
    public static float dpToPx(Resources res, int resId) {
        return res.getDimension(resId) / res.getDisplayMetrics().density;
    }

    public static int getSectionIconDimen(Context context) {
        Resources res = context.getResources();
        return (int) (res.getDimension(R.dimen.common_rect_small)
            / res.getDisplayMetrics().density);
    }

    /**
     * Get the dimensions of a 60x60px.
     *
     * @param context this activities resources
     * @return dimension
     */
    public static int getMenuIconDimen(Context context) {
        return getLargeIconDimen(context);
    }

    /**
     * Get the dimensions of a 48x48px.
     *
     * @param context this activities resources
     * @return dimension
     */
    public static int getLargeIconDimen(Context context) {
        Resources res = context.getResources();
        return (int) (res.getDimension(R.dimen.common_svg_large)
            / res.getDisplayMetrics().density);
    }

    /**
     * Get the dimensions of a 36x36px icon.
     *
     * @param context this activities resources
     * @return dimension
     */
    public static int getMediumIconDimen(Context context) {
        Resources res = context.getResources();
        return (int) (res.getDimension(R.dimen.common_svg_medium)
            / res.getDisplayMetrics().density);
    }

    /**
     * Get the dimensions of a 24x24px icon.
     *
     * @param context this activities resources
     * @return dimension
     */
    public static int getSmallIconDimen(Context context) {
        Resources res = context.getResources();
        return (int) (res.getDimension(R.dimen.common_svg_small)
            / res.getDisplayMetrics().density);
    }

    /**
     * Convert pixels to density pixels.
     *
     * @param context app context
     * @param dp      pixel float value
     * @return float density pixels
     */
    public static float pxToDp(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    /**
     * Get a common {@link com.melnykov.fab.FloatingActionButton} elevation resource.
     *
     * @param context activity context
     * @return elevation dimension
     */
    public static float floatingActionButtonElevation(Context context) {
        return dpToPx(context.getResources(), R.dimen.common_fab_elevation);
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

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

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
        Context context, int resourceId, int backgroundColor) {
        ShapeDrawable background = new ShapeDrawable(new OvalShape());
        background.setColorFilter(backgroundColor, SRC);

        int backgroundRadius = context.getResources()
            .getDimensionPixelSize(R.dimen.common_margin_medium) * 2;
        background.setIntrinsicWidth(backgroundRadius);
        background.setIntrinsicHeight(backgroundRadius);

        BitmapDrawable source = svgToBitmapDrawable(context, resourceId,
            backgroundRadius / 2, Color.WHITE);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{ background, source });
        int inset = backgroundRadius / 2;
        layerDrawable.setLayerInset(1, inset, inset, inset, inset);
        return layerDrawable;
    }

    /**
     * Paint a circular bitmap.
     *
     * @param context         activity context
     * @param source          drawable
     * @param backgroundColor of the bitmap
     * @return the circular bitmap resource
     */
    public static Drawable getCircularDrawableImage(
        Context context, Drawable source, int backgroundColor) {
        ShapeDrawable background = new ShapeDrawable(new OvalShape());
        background.setColorFilter(backgroundColor, SRC);

        int backgroundRadius = context.getResources()
            .getDimensionPixelSize(R.dimen.common_margin_medium);
        background.setIntrinsicWidth(backgroundRadius);
        background.setIntrinsicHeight(backgroundRadius);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{ background, source });
        int inset = backgroundRadius / 2;
        layerDrawable.setLayerInset(1, inset, inset, inset, inset);
        return layerDrawable;
    }

    /**
     * Parse a SVG and return it as a {@link ContentDescriptionDrawable}.
     *
     * @param context    for resources
     * @param resourceId resource ID of the SVG
     * @param size       desired size of the icon
     * @return parsed drawable
     */
    public static Drawable svgToBitmapDrawable(Context context, int resourceId, int size) {
        return svgToBitmapDrawable(context, resourceId, size, NO_COLOR);
    }


    /**
     * Parse a SVG and return it as a {@link ContentDescriptionDrawable}.
     *
     * @param context    for resources
     * @param resourceId resource ID of the SVG
     * @param size       desired size of the icon
     * @param color      desired color of the icon
     * @return parsed drawable
     */
    public static ContentDescriptionDrawable svgToBitmapDrawable(
        Context context, int resourceId, int size, int color) {
        Resources res = context.getResources();
        ContentDescriptionDrawable drawable = null;
        try {
            SVG svg = SVG.getFromResource(res, resourceId);

            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            svg.renderToCanvas(canvas);

            drawable = new ContentDescriptionDrawable(res, bmp);
            if (color != NO_COLOR) {
                drawable.mutate().setColorFilter(color, SRC_IN);
            }
        } catch (SVGParseException e) {
            Timber.e(Log.getStackTraceString(e));
        }
        return drawable;
    }

    /**
     * Get the icon for the specified Activity Intent.
     *
     * @param context     activity context
     * @param packageName name of the activity to find an image for
     * @return activity drawable
     */
    public static Drawable getActivityIcon(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        Drawable icon;
        try {
            icon = pm.getApplicationIcon(packageName);
        } catch (NameNotFoundException e) {
            icon = pm.getDefaultActivityIcon();
            Timber.w("componentName not found, using generic app icon");
        }
        return icon;
    }

    /**
     * Return a new Drawable of the entered resource icon.
     *
     * @param resId icon resource id
     * @return menu icon drawable
     */
    public static Drawable getMenuIcon(Context context, int resId) {
        return svgToBitmapDrawable(context, resId, getLargeIconDimen(context),
            context.getResources().getColor(R.color.common_text_inverse));
    }

}
