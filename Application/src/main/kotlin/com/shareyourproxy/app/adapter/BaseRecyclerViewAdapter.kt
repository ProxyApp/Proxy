package com.shareyourproxy.app.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat.getColor
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.style.TextAppearanceSpan
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.util.ViewUtils.getCircularDrawableImage

/**
 * Base abstraction for all recycler adapters.
 */
abstract class BaseRecyclerViewAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    protected fun getChannelSpannableStringBuilder(context: Context, channelTypeString: String, label: String, address: String): SpannableStringBuilder {
        val sb: SpannableStringBuilder
        val addressStart: Int
        val end: Int
        // there are two different formats for data display, with and without a label
        if (label.length > 0) {
            sb = SpannableStringBuilder(context.getString(
                    R.string.channel_view_item_content, channelTypeString, label, address))
            // add three to account for the formatting ("label - address") hyphen and spacing.
            addressStart = channelTypeString.length + label.length + 3
            end = sb.length

            val labelStart = channelTypeString.length
            val span = TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body)
            sb.setSpan(span, labelStart, addressStart, SPAN_INCLUSIVE_INCLUSIVE)
        } else {
            sb = SpannableStringBuilder(context.getString(
                    R.string.item1_return_item2, channelTypeString, address))
            addressStart = channelTypeString.length
            end = sb.length
        }

        val span = TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body_Disabled)
        sb.setSpan(span, addressStart, end, SPAN_INCLUSIVE_INCLUSIVE)
        return sb
    }

    companion object {

        /**
         * Get a Circular SVG Drawable.
         * @param context                activity context
         * @param channel                resources
         * @param channelBackgroundColor background color value
         * @return circular image.drawable
         */
        fun getChannelIconDrawable(context: Context, channel: Channel, channelBackgroundColor: Int): Drawable {
            return getChannelIconDrawable(context, channel.channelType, channelBackgroundColor)
        }

        /**
         * Get a Circular SVG Drawable.
         * @param context                activity context
         * @param channelType            resId
         * @param channelBackgroundColor background color value
         * @return circular image.drawable
         */
        fun getChannelIconDrawable(context: Context, channelType: ChannelType, channelBackgroundColor: Int): Drawable {
            return getCircularDrawableImage(context, channelType.resId,
                    channelType, channelBackgroundColor)
        }

        /**
         * Get a channel background color based on it's ChannelType.
         * @param context     resrouces
         * @param channelType to switch on
         * @return color int
         */
        fun getChannelBackgroundColor(context: Context, channelType: ChannelType): Int {
            when (channelType) {
                ChannelType.Custom -> return getColor(context, R.color.common_text_secondary)
                ChannelType.Phone -> return getColor(context, R.color.common_indigo)
                ChannelType.SMS -> return getColor(context, R.color.common_light_blue)
                ChannelType.Email -> return getColor(context, R.color.common_red)
                ChannelType.Web, ChannelType.URL -> return getColor(context, R.color.common_blue_dark)
                ChannelType.Facebook -> return getColor(context, R.color.common_facebook)
                ChannelType.Twitter -> return getColor(context, R.color.common_twitter)
                ChannelType.Meerkat -> return getColor(context, R.color.common_meerkat)
                ChannelType.Snapchat -> return getColor(context, R.color.common_snapchat)
                ChannelType.Spotify -> return getColor(context, R.color.common_spotify)
                ChannelType.Reddit -> return getColor(context, R.color.common_reddit)
                ChannelType.Linkedin -> return getColor(context, R.color.common_linkedin)
                ChannelType.FBMessenger -> return getColor(context, R.color.common_fb_messenger)
                ChannelType.Hangouts -> return getColor(context, R.color.common_hangouts)
                ChannelType.Whatsapp -> return getColor(context, R.color.common_whatsapp)
                ChannelType.Yo -> return getColor(context, R.color.common_yo)
                ChannelType.Googleplus -> return getColor(context, R.color.common_google_plus)
                ChannelType.Github -> return getColor(context, R.color.common_github)
                ChannelType.Address -> return getColor(context, R.color.common_address)
                ChannelType.Slack -> return getColor(context, R.color.common_slack)
                ChannelType.Youtube -> return getColor(context, R.color.common_youtube)
                ChannelType.Instagram -> return getColor(context, R.color.common_instagram)
                ChannelType.Tumblr -> return getColor(context, R.color.common_tumblr)
                ChannelType.Ello -> return getColor(context, R.color.common_ello)
                ChannelType.Venmo -> return getColor(context, R.color.common_venmo)
                ChannelType.Periscope -> return getColor(context, R.color.common_periscope)
                ChannelType.Medium -> return getColor(context, R.color.common_medium)
                ChannelType.Soundcloud -> return getColor(context, R.color.common_soundcloud)
                ChannelType.Skype -> return getColor(context, R.color.common_skype)
                ChannelType.LeagueOfLegends -> return getColor(context, R.color.common_leagueoflegends)
                ChannelType.PlaystationNetwork -> return getColor(context, R.color.common_playstation)
                ChannelType.NintendoNetwork -> return getColor(context, R.color.common_nintendo)
                ChannelType.Steam -> return getColor(context, R.color.common_steam)
                ChannelType.Twitch -> return getColor(context, R.color.common_twitch)
                ChannelType.XboxLive -> return getColor(context, R.color.common_xbox)
                else -> return getColor(context, R.color.common_text_secondary)
            }

        }
    }

}
