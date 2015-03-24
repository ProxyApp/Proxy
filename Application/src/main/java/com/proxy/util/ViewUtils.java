package com.proxy.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.proxy.R;
import com.proxy.widget.ContentDescriptionDrawable;

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
            manager.showSoftInput(view, 0);
        }
    }

    /**
     * Convert density pixels to pixels
     *
     * @param context app context
     * @param dp      density pixel float value
     * @return float pixel size value
     */
    public static float dpToPx(Context context, float dp) {
        return dp / context.getResources().getDisplayMetrics().density;
    }


    /**
     * Get the dimensions of a 60x60px.
     *
     * @param activity this activities resources
     * @return dimension
     */
    public static int getMenuIconDimen(Activity activity) {
        Resources res = activity.getResources();
        return (int) (res.getDimension(R.dimen.common_rect_small)
            / res.getDisplayMetrics().density);
    }

    /**
     * Get the dimensions of a 48x48px.
     *
     * @param activity this activities resources
     * @return dimension
     */
    public static int getLargeIconDimen(Activity activity) {
        Resources res = activity.getResources();
        return (int) (res.getDimension(R.dimen.common_svg_large) / res.getDisplayMetrics().density);
    }

    /**
     * Get the dimensions of a 36x36px icon.
     *
     * @param activity this activities resources
     * @return dimension
     */
    public static int getMediumIconDimen(Activity activity) {
        Resources res = activity.getResources();
        return (int) (res.getDimension(R.dimen.common_svg_medium)
            / res.getDisplayMetrics().density);
    }

    /**
     * Get the dimensions of a 24x24px icon.
     *
     * @param activity this activities resources
     * @return dimension
     */
    public static int getSmallIconDimen(Activity activity) {
        Resources res = activity.getResources();
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
     * Paint a circular bitmap.
     *
     * @param resource    Bitmap to crop
     * @param mBitmapPool bitmap pool
     * @return the circular bitmap resource
     */
    public static BitmapResource getCircularBitmapImage(
        Resource<Bitmap> resource, BitmapPool mBitmapPool) {

        Bitmap source = resource.get();
        int size = Math.min(source.getWidth(), source.getHeight());

        int width = (source.getWidth() - size) / 2;
        int height = (source.getHeight() - size) / 2;

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(bitmap,
            BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        bitmap.recycle();
        return BitmapResource.obtain(bitmap, mBitmapPool);
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
        try {
            Resources res = context.getResources();
            SVG svg = SVG.getFromResource(res, resourceId);

            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            svg.renderToCanvas(canvas);

            ContentDescriptionDrawable drawable = new ContentDescriptionDrawable(res, bmp);
            drawable.mutate().setColorFilter(color, SRC_IN);

            return drawable;
        } catch (SVGParseException e) {
            Timber.e(e, TAG + " svgToBitmapDrawable");
        }
        throw new NullPointerException();
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @param <T>       generic object
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }
}
