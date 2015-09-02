package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;

import static android.support.v4.content.ContextCompat.getColor;
import static com.shareyourproxy.util.ViewUtils.getCircularDrawableImage;

/**
 * Base abstraction for all recycler adapters.
 */
public abstract class BaseRecyclerViewAdapter extends RecyclerView.Adapter<BaseViewHolder> {
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
     * Get a channel background color based on it's ChannelType.
     *
     * @param context     resrouces
     * @param channelType to switch on
     * @return color int
     */
    public static int getChannelBackgroundColor(Context context, ChannelType channelType) {
        switch (channelType) {
            case Custom:
                return getColor(context, R.color.common_text_secondary);
            case Phone:
                return getColor(context, R.color.common_indigo);
            case SMS:
                return getColor(context, R.color.common_light_blue);
            case Email:
                return getColor(context, R.color.common_red);
            case Web:
                return getColor(context, R.color.common_text_secondary);
            case Facebook:
                return getColor(context, R.color.common_facebook);
            case Twitter:
                return getColor(context, R.color.common_twitter);
            case Meerkat:
                return getColor(context, R.color.common_meerkat);
            case Snapchat:
                return getColor(context, R.color.common_snapchat);
            case Spotify:
                return getColor(context, R.color.common_spotify);
            case Reddit:
                return getColor(context, R.color.common_reddit);
            case Linkedin:
                return getColor(context, R.color.common_linkedin);
            case FBMessenger:
                return getColor(context, R.color.common_fb_messenger);
            case Hangouts:
                return getColor(context, R.color.common_hangouts);
            case Whatsapp:
                return getColor(context, R.color.common_whatsapp);
            case Yo:
                return getColor(context, R.color.common_yo);
            case Googleplus:
                return getColor(context, R.color.common_google_plus);
            case Github:
                return getColor(context, R.color.common_github);
            case Address:
                return getColor(context, R.color.common_address);
            case Slack:
                return getColor(context, R.color.common_slack);
            case Youtube:
                return getColor(context, R.color.common_youtube);
            case Instagram:
                return getColor(context, R.color.common_instagram);
            case Tumblr:
                return getColor(context, R.color.common_tumblr);
            case Ello:
                return getColor(context, R.color.common_ello);
            case Venmo:
                return getColor(context, R.color.common_venmo);
            case Periscope:
                return getColor(context, R.color.common_periscope);
            case Medium:
                return getColor(context, R.color.common_medium);
            case Soundcloud:
                return getColor(context, R.color.common_soundcloud);
            case Skype:
                return getColor(context, R.color.common_skype);
            default:
                return getColor(context, R.color.common_text_secondary);
        }

    }

}
