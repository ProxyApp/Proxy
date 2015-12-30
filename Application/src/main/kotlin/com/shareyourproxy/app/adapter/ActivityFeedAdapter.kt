package com.shareyourproxy.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.ActivityFeedItem
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.util.bindView
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Feed Adapter.
 * @param feedItems a list of [ActivityFeedItem]s
 */
class ActivityFeedAdapter(recyclerView: BaseRecyclerView, private val contact: User, private val listener: ItemClickListener) : SortedRecyclerAdapter<ActivityFeedItem>(ActivityFeedItem::class.java, recyclerView) {
    val VIEWTYPE_HEADER = 0
    val VIEWTYPE_CONTENT = 1
    private var currentDate = Date()

    fun refreshFeedData(feedItems: List<ActivityFeedItem>?) {
        //get an approximation to "now"
        currentDate = Date()
        refreshData(feedItems)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItemData(position).isError) VIEWTYPE_HEADER else VIEWTYPE_CONTENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_activity_feed_item, parent, false)
        return FeedViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEWTYPE_HEADER -> bindHeaderViewData(holder as FeedViewHolder, getItemData(position))
            VIEWTYPE_CONTENT -> bindFeedViewData(holder as FeedViewHolder, getItemData(position))
        }
    }

    private fun bindHeaderViewData(holder: FeedViewHolder, feedItem: ActivityFeedItem) {
        val context = holder.view.context
        val channelType = feedItem.channelType

        holder.channelImage.setImageDrawable(getChannelIconDrawable(context, channelType, getChannelBackgroundColor(context, channelType)))

        holder.displayText.text = context.getString(R.string.activity_feed_auth_message, channelType, contact.first)
        holder.timestampText.visibility = View.GONE
    }

    override fun compare(item1: ActivityFeedItem, item2: ActivityFeedItem): Int {
        val date1 = item1.timestamp
        val date2 = item2.timestamp
        if (date1 == null) {
            return -1
        } else if (date2 == null) {
            return 1
        } else {
            //reverse chronological
            return -date1.compareTo(date2)
        }
    }

    override fun areContentsTheSame(item1: ActivityFeedItem, item2: ActivityFeedItem): Boolean {
        return item1.actionAddress.equals(item2.actionAddress, true) && item1.channelType.equals(item2.channelType)
    }

    override fun areItemsTheSame(item1: ActivityFeedItem, item2: ActivityFeedItem): Boolean {
        return item1.actionAddress.equals(item2.actionAddress,true) && item1.channelType.equals(item2.channelType)
    }

    /**
     * Set the Channel Intent link content.

     * @param holder [Channel] [BaseViewHolder]
     */
    @SuppressLint("NewApi")
    private fun bindFeedViewData(holder: FeedViewHolder, feedItem: ActivityFeedItem) {
        val context = holder.view.context
        val channelType = feedItem.channelType

        holder.channelImage.setImageDrawable(getChannelIconDrawable(context, channelType, getChannelBackgroundColor(context, channelType)))
        holder.displayText.text = getSpannableString(context, feedItem)
        holder.timestampText.text = getTimePassedString(context, feedItem)
    }

    private fun getSpannableString(context: Context, item: ActivityFeedItem): SpannableStringBuilder {
        val start = item.handle.length
        val sb = SpannableStringBuilder(context.getString(R.string.item1_return_item2, item.handle, item.subtext))
        val span = TextAppearanceSpan(context, R.style.Proxy_TextAppearance_Body)
        sb.setSpan(span, start, sb.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        return sb
    }

    private fun getTimePassedString(context: Context, feedItem: ActivityFeedItem): String {
        val res = context.resources
        val diff = 0L
//        val diff = _currentDate.time.toLong() - feedItem.timestamp?.time?.toLong()
        val diffMinutes = BigDecimal(TimeUnit.MILLISECONDS.toMinutes(diff)).intValueExact()
        val diffHours = BigDecimal(TimeUnit.MILLISECONDS.toHours(diff)).intValueExact()
        val diffDays = BigDecimal(TimeUnit.MILLISECONDS.toDays(diff)).intValueExact()
        val diffYears = diffDays / 365

        if (diffYears > 0) {
            return res.getQuantityString(R.plurals.years_ago, diffYears, diffYears)
        } else if (diffDays > 0) {
            return res.getQuantityString(R.plurals.days_ago, diffDays, diffDays)
        } else if (diffHours > 0) {
            return res.getQuantityString(R.plurals.hours_ago, diffHours, diffHours)
        } else if (diffMinutes > 0) {
            return res.getQuantityString(R.plurals.minutes_ago, diffMinutes, diffMinutes)
        } else {
            return context.getString(R.string.moments_ago)
        }
    }

    /**
     * ViewHolder for the entered [Group] data.
     * @param view              the inflated view
     * @param itemClickListener click listener for each viewholder item
     */
    private final class FeedViewHolder(view: View, itemClickListener: ItemClickListener) : BaseViewHolder(view, itemClickListener) {
        val channelImage: ImageView by bindView(R.id.adapter_activity_feed_item_image)
        val displayText: TextView by bindView(R.id.adapter_activity_feed_item_label)
        val timestampText: TextView by bindView(R.id.adapter_activity_feed_timestamp)
    }
}
