package com.shareyourproxy.app.adapter

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemLongClickListener
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.SHARE_PROFILE
import java.util.*

/**
 * Adapter for a users profile and their [Channel] package permissions.
 */
internal final class ViewChannelAdapter(recyclerView: BaseRecyclerView, sharedPreferences: SharedPreferences, showHeader: Boolean, private val clickListener: ItemLongClickListener) : NotificationRecyclerAdapter<Channel>(Channel::class.java, recyclerView, showHeader, false, sharedPreferences) {

    fun updateChannels(channels: HashMap<String, Channel>?) {
        refreshData(channels?.values)
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_channel_view_content, parent, false)
        return ContentViewHolder(view, clickListener)
    }

    override fun compare(item1: Channel, item2: Channel): Int {
        val weight1 = item1.channelType.weight
        val weight2 = item2.channelType.weight
        val compareFirst = compareValues(weight1, weight2)
        if (compareFirst == 0 || (weight1 > 4 && weight2 > 4)) {
            val label1 = item1.label
            val label2 = item2.label
            val compareSecond = label1.compareTo(label2)
            if (compareSecond == 0) {
                val action1 = item1.actionAddress
                val action2 = item2.actionAddress
                return action1.compareTo(action2)
            } else {
                return compareSecond
            }
        } else {
            return compareFirst
        }
    }

    override fun areContentsTheSame(item1: Channel, item2: Channel): Boolean {
        return (item1.id.equals(item2.id) && item1.label.equals(item2.label) && item1.channelType.equals(item2.channelType)) && item1.actionAddress.equals(item2.actionAddress)
    }

    override fun areItemsTheSame(item1: Channel, item2: Channel): Boolean {
        return item1.id.equals(item2.id)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is NotificationRecyclerAdapter.HeaderViewHolder) {
            bindHeaderViewData(holder, SHARE_PROFILE, true, false)
        } else if (holder is ContentViewHolder) {
            bindContentViewData(holder, getItemData(position))
        }
    }

    /**
     * Set the Channel Intent link content.
     * @param holder  [Channel] [BaseViewHolder]
     * @param channel [Channel] data
     */
    private fun bindContentViewData(holder: ContentViewHolder, channel: Channel) {
        val context = holder.view.context
        val channelType = channel.channelType
        val channelTypeString = channel.channelType.label
        val label = channel.label
        val address = channel.actionAddress
        val sb = getChannelSpannableStringBuilder(context, channelTypeString, label, address)

        holder.channelImage.setImageDrawable(getChannelIconDrawable(context, channel, getChannelBackgroundColor(context, channelType)))
        holder.channelContentText.text = sb
    }

    /**
     * ViewHolder for the entered settings data.
     */
    private final class ContentViewHolder(view: View, itemClickListener: ItemLongClickListener) : BaseViewHolder(view, itemClickListener) {
        val channelImage: ImageView by bindView(R.id.adapter_channel_view_content_image)
        val channelContentText: TextView by bindView(R.id.adapter_channel_view_content)
    }
}