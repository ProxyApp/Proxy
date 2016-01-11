package com.shareyourproxy.app.adapter

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import com.shareyourproxy.R
import com.shareyourproxy.R.id.adapter_dismissible_notification_card
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay.rxBusObservable
import com.shareyourproxy.api.rx.event.NotificationCardDismissEvent
import com.shareyourproxy.app.adapter.NotificationRecyclerAdapter.NotificationViewHolder.Companion.TYPE_FOOTER
import com.shareyourproxy.app.adapter.NotificationRecyclerAdapter.NotificationViewHolder.Companion.TYPE_HEADER
import com.shareyourproxy.app.adapter.NotificationRecyclerAdapter.NotificationViewHolder.Companion.TYPE_LIST_ITEM
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.widget.DismissibleNotificationCard
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard

/**
 * Adapter that can handle displaying a dismissable notification card as a header, footer or both.
 */
internal abstract class NotificationRecyclerAdapter<T>(clazz: Class<T>, recyclerView: BaseRecyclerView, showHeader: Boolean, showFooter: Boolean, private val _prefs: SharedPreferences) : SortedRecyclerAdapter<T>(clazz, recyclerView) {
    private val busObserver: JustObserver<Any> = object : JustObserver<Any>(NotificationRecyclerAdapter::class.java) {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            if (event is NotificationCardDismissEvent) {
                removeNotificationCard(_prefs, event)
            }
        }
    }
    private var headerCard: NotificationCard? = null
    private var footerCard: NotificationCard? = null
    var isHeaderVisible = showHeader
        private set
    var isFooterVisible = showFooter
        private set

    init {
        rxBusObservable().subscribe(busObserver)
    }

    internal fun removeNotificationCard(prefs: SharedPreferences, event: NotificationCardDismissEvent) {
        if (event.adapter.javaClass == this.javaClass) {
            if (event.holder is HeaderViewHolder) {
                isHeaderVisible = false
                notifyItemRemoved(0)
            } else if (event.holder is FooterViewHolder) {
                isFooterVisible = false
                notifyItemRemoved(itemCount - 1)
            }
            prefs.edit().putBoolean(event.cardType.key, true).apply()
        }
    }

    internal fun bindHeaderViewData(holder: HeaderViewHolder, cardType: NotificationCard, showDismiss: Boolean, showAction: Boolean) {
        headerCard = cardType
        if (isHeaderVisible) {
            holder.notificationCard.createNotificationCard(this, holder, cardType, showDismiss, showAction)
        } else {
            holder.view.visibility = GONE
        }
    }

    internal fun bindFooterViewData(holder: FooterViewHolder, cardType: NotificationCard, showDismiss: Boolean, showAction: Boolean) {
        if (isFooterVisible) {
            footerCard = cardType
            holder.notificationCard.createNotificationCard(this, holder, cardType, showDismiss, showAction)
        } else {
            holder.view.visibility = GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        when (viewType) {
            TYPE_HEADER -> return headerViewHolder(parent)
            TYPE_FOOTER -> return footerViewHolder(parent)
            else -> return onCreateItemViewHolder(parent, viewType)
        }
    }

    private fun footerViewHolder(parent: ViewGroup): FooterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_dismissible_notification, parent, false)
        return FooterViewHolder(view, null)
    }

    private fun headerViewHolder(parent: ViewGroup): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_dismissible_notification, parent, false)
        return HeaderViewHolder(view, null)
    }

    override fun getItemCount(): Int = totalItemCount

    private val totalItemCount: Int get() {
        var listSize = staticListSize

        if (listSize == 0) {
            return 0
        }
        if (isHeaderVisible) {
            ++listSize
        }
        if (isFooterVisible) {
            ++listSize
        }
        return listSize
    }

    override fun getItemViewType(position: Int): Int {
        return checkItemViewType(position)
    }

    override fun getItemData(position: Int): T {
        return super.getItemData(getDataPositionOffset(position))
    }

    override fun removeItem(position: Int) {
        super.removeItem(getDataPositionOffset(position))
    }

    override fun onInserted(position: Int, count: Int) {
        super.onInserted(getViewPositionOffset(position), count)
    }

    override fun onRemoved(position: Int, count: Int) {
        super.onRemoved(getViewPositionOffset(position), count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        super.onMoved(getViewPositionOffset(fromPosition),
                getViewPositionOffset(toPosition))
    }

    override fun onChanged(position: Int, count: Int) {
        super.onChanged(getViewPositionOffset(position), count)
    }

    override fun beforeDataSetChanged(position: Int, count: Int) {
        if (headerCard != null) {
            isHeaderVisible = !_prefs.getBoolean(headerCard!!.key, false)
        }
        if (footerCard != null) {
            isFooterVisible = !_prefs.getBoolean(footerCard!!.key, false)
        }
    }

    private fun getDataPositionOffset(position: Int): Int {
        if (isHeaderVisible || isFooterVisible) {
            val offset = position - 1
            val end = totalItemCount - 1
            return if (offset > 0) if (offset == totalItemCount) end else offset else 0
        } else {
            return position
        }
    }

    private fun getViewPositionOffset(position: Int): Int {
        if (isHeaderVisible) {
            return position + 1
        } else {
            return position
        }
    }

    private fun checkItemViewType(position: Int): Int {
        if (position == 0 && isHeaderVisible) {
            return TYPE_HEADER
        } else if (position == (totalItemCount - 1) && isFooterVisible) {
            return TYPE_FOOTER
        } else {
            return TYPE_LIST_ITEM
        }
    }

    protected abstract fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder

    /**
     * ViewHolder for the lists header.
     */
    internal final class HeaderViewHolder(view: View, itemClickListener: BaseViewHolder.ItemClickListener?) : NotificationViewHolder(view, itemClickListener) {
    }

    /**
     * ViewHolder for the lists footer.
     */
    internal final class FooterViewHolder(view: View, itemClickListener: BaseViewHolder.ItemClickListener?) : NotificationViewHolder(view, itemClickListener) {
    }

    /**
     * ViewHolder for switching on type.
     */
    open class NotificationViewHolder(view: View, itemClickListener: BaseViewHolder.ItemClickListener?) : BaseViewHolder(view, itemClickListener) {
        companion object {
            val TYPE_HEADER = 0
            val TYPE_LIST_ITEM = 1
            val TYPE_FOOTER = 2
        }

        internal val notificationCard: DismissibleNotificationCard by bindView(adapter_dismissible_notification_card)
    }
}
