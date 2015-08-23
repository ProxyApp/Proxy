package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;

import static com.shareyourproxy.util.ViewUtils.getCircularDrawableImage;
import static com.shareyourproxy.util.ViewUtils.getSectionIconDimen;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;

/**
 * Base abstraction for all recycler adapters.
 */
public abstract class BaseRecyclerViewAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    /**
     * Parse a svg and return a Large sized {@link Drawable}.
     *
     * @param context    activity context
     * @param resourceId resource to create image.drawable
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
     * @param context                activity context
     * @param channel                resources
     * @param channelBackgroundColor background color value
     * @return circular image.drawable
     */
    public static Drawable getSVGIconDrawable(
        Context context, Channel channel, int channelBackgroundColor) {
        return getCircularDrawableImage(context, channel.channelType().getResId(),
            channel.channelType(), channelBackgroundColor);
    }

    /**
     * Get a Circular Android Icon Drawable.
     *
     * @param context  activity context
     * @param drawable source image.drawable
     * @return circular image.drawable
     */
    public static Drawable getAndroidIconDrawable(Context context, Drawable drawable) {
        return getCircularDrawableImage(context, drawable, Color.LTGRAY);
    }

    public static int getChannelBackgroundColor(Context context, ChannelType channelType) {
        Resources res = context.getResources();
        switch (channelType) {
            case Custom:
                return res.getColor(R.color.common_text_secondary);
            case Phone:
                return res.getColor(R.color.common_indigo);
            case SMS:
                return res.getColor(R.color.common_light_blue);
            case Email:
                return res.getColor(R.color.common_red);
            case Web:
                return res.getColor(R.color.common_text_secondary);
            case Facebook:
                return res.getColor(R.color.common_facebook);
            case Twitter:
                return res.getColor(R.color.common_twitter);
            case Meerkat:
                return res.getColor(R.color.common_meerkat);
            case Snapchat:
                return res.getColor(R.color.common_snapchat);
            case Spotify:
                return res.getColor(R.color.common_spotify);
            case Reddit:
                return res.getColor(R.color.common_reddit);
            case Linkedin:
                return res.getColor(R.color.common_linkedin);
            case FBMessenger:
                return res.getColor(R.color.common_fb_messenger);
            case Hangouts:
                return res.getColor(R.color.common_hangouts);
            case Whatsapp:
                return res.getColor(R.color.common_whatsapp);
            case Yo:
                return res.getColor(R.color.common_yo);
            case Googleplus:
                return res.getColor(R.color.common_google_plus);
            case Github:
                return res.getColor(R.color.common_github);
            case Address:
                return res.getColor(R.color.common_address);
            case Slack:
                return res.getColor(R.color.common_slack);
            case Youtube:
                return res.getColor(R.color.common_youtube);
            case Instagram:
                return res.getColor(R.color.common_instagram);
            case Tumblr:
                return res.getColor(R.color.common_tumblr);
            case Ello:
                return res.getColor(R.color.common_ello);
            case Venmo:
                return res.getColor(R.color.common_venmo);
            case Periscope:
                return res.getColor(R.color.common_periscope);
            case Medium:
                return res.getColor(R.color.common_medium);
            case Soundcloud:
                return res.getColor(R.color.common_soundcloud);
            case Skype:
                return res.getColor(R.color.common_skype);
            default:
                return res.getColor(R.color.common_text_secondary);
        }

    }

}
