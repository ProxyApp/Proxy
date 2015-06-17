package com.shareyourproxy.widget.transform;

import android.graphics.Bitmap;

import com.shareyourproxy.util.ViewUtils;
import com.squareup.picasso.Transformation;

/**
 * Create a bitmap drawable with a transparent black background placed on top of it.
 */
public class AlphaTransform implements Transformation{

    /**
     * Static new instance.
     *
     * @return Circular Transform
     */
    public static AlphaTransform create() {
        return new AlphaTransform();
    }

    @Override
    public Bitmap transform(Bitmap source) {
        return ViewUtils.getAlphaBitmapImage(source);
    }

    @Override
    public String key() {
        return "AlphaTransformation()";
    }
}
