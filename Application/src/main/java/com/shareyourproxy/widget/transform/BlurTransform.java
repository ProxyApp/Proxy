package com.shareyourproxy.widget.transform;

import android.content.Context;
import android.graphics.Bitmap;

import com.shareyourproxy.util.ViewUtils;
import com.squareup.picasso.Transformation;

/**
 * Transformation that blurs a source bitmap image.
 */
public class BlurTransform implements Transformation {


    private final Context _context;

    public BlurTransform(Context context) {
        _context = context;
    }

    /**
     * Static new instance
     *
     * @return Circular Transform
     */
    public static BlurTransform create(Context context) {
        return new BlurTransform(context);
    }

    @Override
    public Bitmap transform(Bitmap source) {
        return ViewUtils.getBlurBitmapImage(_context, source);
    }

    @Override
    public String key() {
        return "BlurTransformation()";
    }
}
