package com.shareyourproxy.app.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat.getColor
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.style.TextAppearanceSpan
import com.shareyourproxy.R.color.*
import com.shareyourproxy.R.string.channel_view_item_content
import com.shareyourproxy.R.string.item1_return_item2
import com.shareyourproxy.R.style.Proxy_TextAppearance_Body
import com.shareyourproxy.R.style.Proxy_TextAppearance_Body_Disabled
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.api.domain.model.ChannelType.*
import com.shareyourproxy.util.ViewUtils.getCircularDrawableImage

/**
 * Base abstraction for all recycler adapters.
 */
internal abstract class BaseRecyclerViewAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    protected fun getChannelSpannableStringBuilder(context: Context, channelTypeString: String, label: String, address: String): SpannableStringBuilder {
        val sb: SpannableStringBuilder
        val addressStart: Int
        val end: Int
        // there are two different formats for data display, with and without a label
        if (label.length > 0) {
            sb = SpannableStringBuilder(context.getString(channel_view_item_content, channelTypeString, label, address))
            // add three to account for the formatting ("label - address") hyphen and spacing.
            addressStart = channelTypeString.length + label.length + 3
            end = sb.length

            val labelStart = channelTypeString.length
            val span = TextAppearanceSpan(context, Proxy_TextAppearance_Body)
            sb.setSpan(span, labelStart, addressStart, SPAN_INCLUSIVE_INCLUSIVE)
        } else {
            sb = SpannableStringBuilder(context.getString(item1_return_item2, channelTypeString, address))
            addressStart = channelTypeString.length
            end = sb.length
        }

        val span = TextAppearanceSpan(context, Proxy_TextAppearance_Body_Disabled)
        sb.setSpan(span, addressStart, end, SPAN_INCLUSIVE_INCLUSIVE)
        return sb
    }

    /**
     * Get a Circular SVG Drawable.
     * @param context                activity context
     * @param channel                resources
     * @param channelBackgroundColor background color value
     * @return circular image.drawable
     */
    protected fun getChannelIconDrawable(context: Context, channel: Channel, channelBackgroundColor: Int): Drawable {
        return getChannelIconDrawable(context, channel.channelType, channelBackgroundColor)
    }

    /**
     * Get a Circular SVG Drawable.
     * @param context                activity context
     * @param channelType            resId
     * @param channelBackgroundColor background color value
     * @return circular image.drawable
     */
    protected fun getChannelIconDrawable(context: Context, channelType: ChannelType, channelBackgroundColor: Int): Drawable {
        return getCircularDrawableImage(context, channelType.resId, channelType, channelBackgroundColor)
    }

    /**
     * Get a channel background color based on it's ChannelType.
     * @param context     resrouces
     * @param channelType to switch on
     * @return color int
     */
    protected fun getChannelBackgroundColor(context: Context, channelType: ChannelType): Int {
        return when (channelType) {
            Custom -> getColor(context, common_text_secondary)
            Phone -> getColor(context, common_indigo)
            SMS -> getColor(context, common_light_blue)
            Email -> getColor(context, common_red)
            Web, URL -> getColor(context, common_blue_dark)
            Facebook -> getColor(context, common_facebook)
            Twitter -> getColor(context, common_twitter)
            Meerkat -> getColor(context, common_meerkat)
            Snapchat -> getColor(context, common_snapchat)
            Spotify -> getColor(context, common_spotify)
            Reddit -> getColor(context, common_reddit)
            Linkedin -> getColor(context, common_linkedin)
            FBMessenger -> getColor(context, common_fb_messenger)
            Hangouts -> getColor(context, common_hangouts)
            Whatsapp -> getColor(context, common_whatsapp)
            Yo -> getColor(context, common_yo)
            Googleplus -> getColor(context, common_google_plus)
            Github -> getColor(context, common_github)
            Address -> getColor(context, common_address)
            Slack -> getColor(context, common_slack)
            Youtube -> getColor(context, common_youtube)
            Instagram -> getColor(context, common_instagram)
            Tumblr -> getColor(context, common_tumblr)
            Ello -> getColor(context, common_ello)
            Venmo -> getColor(context, common_venmo)
            Periscope -> getColor(context, common_periscope)
            Medium -> getColor(context, common_medium)
            Soundcloud -> getColor(context, common_soundcloud)
            Skype -> getColor(context, common_skype)
            LeagueOfLegends -> getColor(context, common_leagueoflegends)
            PlaystationNetwork -> getColor(context, common_playstation)
            NintendoNetwork -> getColor(context, common_nintendo)
            Steam -> getColor(context, common_steam)
            Twitch -> getColor(context, common_twitch)
            XboxLive -> getColor(context, common_xbox)
            else -> getColor(context, common_text_secondary)
        }
    }

    /**
     * Combine both functions to create a channel icon.
     */
    protected fun getChannelDrawable(channel: Channel, channelType: ChannelType, context: Context) =
            getChannelIconDrawable(context, channel, getChannelBackgroundColor(context, channelType))
}
