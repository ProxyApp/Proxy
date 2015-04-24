package com.proxy.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.proxy.R;
import com.proxy.widget.ContentDescriptionDrawable;
import com.proxy.widget.FloatingActionButton;

import timber.log.Timber;

import static android.content.Context.INPUT_METHOD_SERVICE;
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


    /**
     * Get the dimensions of a 60x60px.
     *
     * @param context this activities resources
     * @return dimension
     */
    public static int getMenuIconDimen(Context context) {
        Resources res = context.getResources();
        return (int) (res.getDimension(R.dimen.common_rect_small)
            / res.getDisplayMetrics().density);
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
     * Get a common {@link FloatingActionButton} elevation resource.
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

        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP,
            BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return bitmap;
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

}
