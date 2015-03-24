package com.proxy.widget.transform;

import android.graphics.Bitmap;

import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.proxy.util.ViewUtils;

/**
 * {@link Transformation} that returns a circular cropped image.
 */
public class GlideCircleTransform implements Transformation<Bitmap> {
    private BitmapPool mBitmapPool;

    /**
     * Constructor.
     *
     * @param pool bitmap pool
     */
    private GlideCircleTransform(BitmapPool pool) {
        this.mBitmapPool = pool;
    }

    /**
     * Static new instance
     *
     * @param pool bitmap pool
     * @return Circular Transform
     */
    public static GlideCircleTransform create(BitmapPool pool) {
        return new GlideCircleTransform(pool);
    }

    @Override
    public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
        return ViewUtils.getCircularBitmapImage(resource, mBitmapPool);
    }

    @Override
    public String getId() {
        return "CropCircleTransformation()";
    }
}
