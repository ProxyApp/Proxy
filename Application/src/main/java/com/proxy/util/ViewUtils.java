package com.proxy.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import static android.content.Context.INPUT_METHOD_SERVICE;
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

    public static Bitmap getCircularBitmapImage(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
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
}
