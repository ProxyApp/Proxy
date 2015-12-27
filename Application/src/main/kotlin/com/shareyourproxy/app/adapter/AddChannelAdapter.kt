package com.shareyourproxy.app.adapter

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.api.domain.model.ChannelType.*
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.util.ObjectUtils.capitalize
import com.shareyourproxy.util.bindView
import com.shareyourproxy.widget.DismissibleNotificationCard
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.CUSTOM_URL
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.SAFE_INFO
import java.util.*

/**
 * Adapter that handles displaying channels.
 */
class AddChannelAdapter(
        showHeader: Boolean, showFooter: Boolean, recyclerView: BaseRecyclerView,
        sharedPreferences: SharedPreferences, private val _clickListener: ItemClickListener) : NotificationRecyclerAdapter<Channel>(Channel::class.java, recyclerView, showHeader, showFooter, sharedPreferences) {

    internal var channelList = Arrays.asList(
            PHONE, SMS, EMAIL, WEB, FACEBOOK, TWITTER, MEERKAT, REDDIT, LINKEDIN,
            GOOGLEPLUS, GITHUB, ADDRESS, YOUTUBE, INSTAGRAM, TUMBLR, ELLO,
            VENMO, MEDIUM, SOUNDCLOUD, SKYPE, SNAPCHAT, WHATSAPP, LEAGUEOFLEGENDS,
            PLAYSTATIONNETWORK, NINTENDONETWORK, STEAM, TWITCH, XBOXLIVE)

    init {
        refreshData(channelList)
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_add_channel_list_item, parent, false)
        return ItemViewHolder.newInstance(view, _clickListener)
    }

    override fun compare(item1: Channel, item2: Channel): Int {
        return sortAlpha_WebToBottom(item1, item2)
    }

    fun sortAlpha_WebToBottom(item1: Channel, item2: Channel): Int {
        val isWeb1 = (item1.channelType.equals(ChannelType.Web) || item1.channelType.equals(ChannelType.URL))
        val isWeb2 = (item2.channelType.equals(ChannelType.Web) || item2.channelType.equals(ChannelType.URL))

        val label1 = item1.channelType.label
        val label2 = item2.channelType.label

        val weight1 = item1.channelType.weight
        val weight2 = item2.channelType.weight
        val compareFirst = compareValues(weight1, weight2)

        if (isWeb1 && isWeb2) {
            return 0
        } else {
            if (compareFirst == 0 || (weight1 > 3 && weight2 > 3)) {
                if (isWeb2) {
                    return -1
                } else {
                    return label1.compareTo(label2)
                }
            } else {
                return compareFirst
            }
        }
    }

    override fun areContentsTheSame(item1: Channel, item2: Channel): Boolean {
        return (item1.id.equals(item2.id) && item1.label.equals(item2.label) && item1.channelType.equals(item2.channelType))
    }

    override fun areItemsTheSame(item1: Channel, item2: Channel): Boolean {
        return item1.id.equals(item2.id)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is NotificationRecyclerAdapter.HeaderViewHolder) {
            bindHeaderViewData(holder, SAFE_INFO, true, false)
        } else if (holder is NotificationRecyclerAdapter.FooterViewHolder) {
            bindFooterViewData(holder, CUSTOM_URL, true, false)
        } else {
            bindItemViewData(holder as ItemViewHolder, getItemData(position))
        }
    }

    /**
     * Set the Channel Intent link content.

     * @param holder  [Channel] [BaseViewHolder]
     * *
     * @param channel [Channel] data
     */
    @SuppressLint("NewApi")
    private fun bindItemViewData(holder: ItemViewHolder, channel: Channel) {
        val context = holder.view.context
        val channelType = channel.channelType
        holder.itemImage.setImageDrawable(
                BaseRecyclerViewAdapter.getChannelIconDrawable(context, channel,
                        BaseRecyclerViewAdapter.getChannelBackgroundColor(context, channelType)))
        holder.itemLabel.text = capitalize(channel.label)
    }

    /**
     * ViewHolder for the entered settings data.
     * @param view              the inflated view
     * @param itemClickListener click listener for this view
     */
    class ItemViewHolder
    private constructor(view: View, itemClickListener: ItemClickListener) : BaseViewHolder(view, itemClickListener) {
        val itemImage: ImageView by bindView(R.id.adapter_add_channel_list_item_image)
        val itemLabel: TextView by bindView(R.id.adapter_add_channel_list_item_label)

        companion object {
            fun newInstance(view: View, itemClickListener: ItemClickListener): ItemViewHolder {
                return ItemViewHolder(view, itemClickListener)
            }
        }
    }

    companion object {
        private val PHONE = createModelInstance(Phone)
        private val SMS = createModelInstance(ChannelType.SMS)
        private val EMAIL = createModelInstance(Email)
        private val WEB = createModelInstance(Web)
        private val FACEBOOK = createModelInstance(Facebook)
        private val TWITTER = createModelInstance(Twitter)
        private val MEERKAT = createModelInstance(Meerkat)
        private val REDDIT = createModelInstance(Reddit)
        private val LINKEDIN = createModelInstance(Linkedin)
        private val GOOGLEPLUS = createModelInstance(Googleplus)
        private val GITHUB = createModelInstance(Github)
        private val ADDRESS = createModelInstance(Address)
        private val YOUTUBE = createModelInstance(Youtube)
        private val INSTAGRAM = createModelInstance(Instagram)
        private val TUMBLR = createModelInstance(Tumblr)
        private val ELLO = createModelInstance(Ello)
        private val VENMO = createModelInstance(Venmo)
        private val MEDIUM = createModelInstance(Medium)
        private val SOUNDCLOUD = createModelInstance(Soundcloud)
        private val SKYPE = createModelInstance(Skype)
        private val SNAPCHAT = createModelInstance(Snapchat)
        private val WHATSAPP = createModelInstance(Whatsapp)
        private val LEAGUEOFLEGENDS = createModelInstance(LeagueOfLegends)
        private val PLAYSTATIONNETWORK = createModelInstance(PlaystationNetwork)
        private val NINTENDONETWORK = createModelInstance(NintendoNetwork)
        private val STEAM = createModelInstance(Steam)
        private val TWITCH = createModelInstance(Twitch)
        private val XBOXLIVE = createModelInstance(XboxLive)

        /**
         * Create a newInstance of a [AddChannelAdapter] with blank data.
         * @return an [AddChannelAdapter] with no data
         */
        fun newInstance(recyclerView: BaseRecyclerView, sharedPreferences: SharedPreferences, listener: ItemClickListener): AddChannelAdapter {
            val showHeader = !sharedPreferences.getBoolean(DismissibleNotificationCard.NotificationCard.SAFE_INFO.key, false)
            val showFooter = !sharedPreferences.getBoolean(DismissibleNotificationCard.NotificationCard.CUSTOM_URL.key, false)
            return AddChannelAdapter(showHeader, showFooter, recyclerView, sharedPreferences, listener)
        }
    }
}

