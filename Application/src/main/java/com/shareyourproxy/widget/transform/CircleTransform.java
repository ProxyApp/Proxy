package com.shareyourproxy.widget.transform;

import android.graphics.Bitmap;

import com.shareyourproxy.util.ViewUtils;
import com.squareup.picasso.Transformation;

/**
 * {@link Transformation} that returns a circular cropped image.
 */
public class CircleTransform implements Transformation {

    /**
     * Static new instance
     *
     * @return Circular Transform
     */
    public static CircleTransform create() {
        return new CircleTransform();
    }

    @Override
    public Bitmap transform(Bitmap source) {
        return ViewUtils.getCircularBitmapImage(source);
    }

    @Override
    public String key() {
        return "CropCircleTransformation()";
    }
}
