package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;

import static android.support.v4.content.ContextCompat.getColor;
import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;
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
    protected static Drawable getChannelIconDrawable(
        Context context, Channel channel, int channelBackgroundColor) {
        return getChannelIconDrawable(context, channel.channelType(), channelBackgroundColor);
    }

    /**
     * Get a Circular SVG Drawable.
     *
     * @param context                activity context
     * @param channelType            resId
     * @param channelBackgroundColor background color value
     * @return circular image.drawable
     */
    protected static Drawable getChannelIconDrawable(
        Context context, ChannelType channelType, int channelBackgroundColor) {
        return getCircularDrawableImage(context, channelType.getResId(),
            channelType, channelBackgroundColor);
    }

    /**
     * Get a channel background color based on it's ChannelType.
     *
     * @param context     resrouces
     * @param channelType to switch on
     * @return color int
     */
    protected static int getChannelBackgroundColor(Context context, ChannelType channelType) {
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
            case URL:
                return getColor(context, R.color.common_blue_dark);
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
            case LeagueOfLegends:
                return getColor(context, R.color.common_leagueoflegends);
            case PlaystationNetwork:
                return getColor(context, R.color.common_playstation);
            case NintendoNetwork:
                return getColor(context, R.color.common_nintendo);
            case Steam:
                return getColor(context, R.color.common_steam);
            case Twitch:
                return getColor(context, R.color.common_twitch);
            case XboxLive:
                return getColor(context, R.color.common_xbox);
            default:
                return getColor(context, R.color.common_text_secondary);
        }

    }

    protected SpannableStringBuilder getChannelSpannableStringBuilder(
        Context context, String channelTypeString, String label, String address) {
        SpannableStringBuilder sb;
        int addressStart;
        int end;
        // there are two different formats for data display, with and without a label
        if (label.length() > 0) {
            sb = new SpannableStringBuilder(context.getString(
                R.string.channel_view_item_content, channelTypeString, label, address));
            // add three to account for the formatting ("label - address") hyphen and spacing.
            addressStart = channelTypeString.length() + label.length() + 3;
            end = sb.length();

            int labelStart = channelTypeString.length();
            TextAppearanceSpan span =
                new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body);
            sb.setSpan(span, labelStart, addressStart, SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            sb = new SpannableStringBuilder(context.getString(
                R.string.item1_return_item2, channelTypeString, address));
            addressStart = channelTypeString.length();
            end = sb.length();
        }

        TextAppearanceSpan span =
            new TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body_Disabled);
        sb.setSpan(span, addressStart, end, SPAN_INCLUSIVE_INCLUSIVE);
        return sb;
    }

}
