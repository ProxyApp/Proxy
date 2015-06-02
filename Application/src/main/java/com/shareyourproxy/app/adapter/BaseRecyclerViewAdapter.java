package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;

import com.shareyourproxy.R;

import static com.shareyourproxy.util.ViewUtils.getCircularDrawableImage;
import static com.shareyourproxy.util.ViewUtils.getSectionIconDimen;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Created by Evan on 5/5/15.
 */
public abstract class BaseRecyclerViewAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    /**
     * Parse a svg and return a Large sized {@link Drawable}.
     *
     * @param context    activity context
     * @param resourceId resource to create drawable
     * @return Drawable
     */
    public static Drawable getSectionResourceDrawable(Context context, int resourceId) {
        Resources res = context.getResources();
        return svgToBitmapDrawable(context, resourceId,
            getSectionIconDimen(context), res.getColor(R.color.common_text));
    }

    /**
     * Get a Circular SVG Drawable.
     *
     * @param context    activity context
     * @param resourceId resource to decode
     * @return circular drawable
     */
    public static Drawable getSVGIconDrawable(Context context, int resourceId) {
        Resources res = context.getResources();
        return getCircularDrawableImage(context, resourceId,
            res.getColor(R.color.common_text_secondary));
    }

    /**
     * Get a Circular Android Icon Drawable.
     *
     * @param context  activity context
     * @param drawable source drawable
     * @return circular drawable
     */
    public static Drawable getAndroidIconDrawable(Context context, Drawable drawable) {
        return getCircularDrawableImage(context, drawable, Color.LTGRAY);
    }
}
