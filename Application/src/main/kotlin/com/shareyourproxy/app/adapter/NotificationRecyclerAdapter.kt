package com.shareyourproxy.app.adapter

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import butterknife.bindView
import com.shareyourproxy.R
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusDriver
import com.shareyourproxy.api.rx.event.NotificationCardDismissEvent
import com.shareyourproxy.widget.DismissibleNotificationCard
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard

/**
 * Adapter that can handle displaying a dismissable notification card as a header, footer or both.
 */
abstract class NotificationRecyclerAdapter<T>(clazz: Class<T>, recyclerView: BaseRecyclerView, showHeader: Boolean, showFooter: Boolean,
                                              private val _prefs: SharedPreferences) : SortedRecyclerAdapter<T>(clazz, recyclerView) {
    var isHeaderVisible = false
        private set
    var isFooterVisible = false
        private set
    private var _headerCard: NotificationCard? = null
    private var _footerCard: NotificationCard? = null

    init {
        isHeaderVisible = showHeader
        isFooterVisible = showFooter
        RxBusDriver.rxBusObservable().subscribe(busObserver)
    }

    val busObserver: JustObserver<Any> get() = object : JustObserver<Any>() {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any?) {
            if (event is NotificationCardDismissEvent) {
                removeNotificationCard(_prefs, event)
            }
        }
    }

    fun removeNotificationCard(prefs: SharedPreferences, event: NotificationCardDismissEvent) {
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

    fun bindHeaderViewData(holder: HeaderViewHolder, cardType: NotificationCard, showDismiss: Boolean, showAction: Boolean) {
        _headerCard = cardType
        if (isHeaderVisible) {
            holder.notificationCard.createNotificationCard(this, holder, cardType, showDismiss, showAction)
        } else {
            holder.view.visibility = GONE
        }
    }

    fun bindFooterViewData(holder: FooterViewHolder, cardType: NotificationCard, showDismiss: Boolean, showAction: Boolean) {
        if (isFooterVisible) {
            _footerCard = cardType
            holder.notificationCard.createNotificationCard(this, holder, cardType, showDismiss, showAction)
        } else {
            holder.view.visibility = GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_dismissible_notification, parent, false)
            return HeaderViewHolder.newInstance(view, null)
        } else if (viewType == TYPE_FOOTER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_dismissible_notification, parent, false)
            return FooterViewHolder.newInstance(view, null)
        } else {
            return onCreateItemViewHolder(parent, viewType)
        }
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

    public override fun onMoved(fromPosition: Int, toPosition: Int) {
        super.onMoved(getViewPositionOffset(fromPosition),
                getViewPositionOffset(toPosition))
    }

    public override fun onChanged(position: Int, count: Int) {
        super.onChanged(getViewPositionOffset(position), count)
    }

    override fun beforeDataSetChanged(position: Int, count: Int) {
        if (_headerCard != null) {
            isHeaderVisible = !_prefs.getBoolean(_headerCard!!.key, false)
        }
        if (_footerCard != null) {
            isFooterVisible = !_prefs.getBoolean(_footerCard!!.key, false)
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
    class HeaderViewHolder
    /**
     * Constructor for the HeaderViewHolder.
     * @param view              the inflated view
     * @param itemClickListener click listener for this view
     */
    private constructor(view: View, itemClickListener: BaseViewHolder.ItemClickListener?) : NotificationViewHolder(view, itemClickListener) {
        companion object {

            /**
             * Create a new Instance of the ViewHolder.
             * @param view              inflated in [RecyclerView.Adapter.onCreateViewHolder]
             * @param itemClickListener click listener for this view
             * @return a ViewHolder instance
             */
            fun newInstance(view: View, itemClickListener: BaseViewHolder.ItemClickListener?): HeaderViewHolder {
                return HeaderViewHolder(view, itemClickListener)
            }
        }
    }

    /**
     * ViewHolder for the lists footer.
     */
    class FooterViewHolder
    /**
     * Constructor for the FooterViewHolder.
     * @param view              the inflated view
     * @param itemClickListener click listener for this view
     */
    private constructor(view: View, itemClickListener: BaseViewHolder.ItemClickListener?) : NotificationViewHolder(view, itemClickListener) {
        companion object {

            /**
             * Create a new Instance of the ViewHolder.
             * @param view              inflated in [RecyclerView.Adapter.onCreateViewHolder]
             * @param itemClickListener click listener for this view
             * @return a ViewHolder instance
             */
            fun newInstance(view: View, itemClickListener: BaseViewHolder.ItemClickListener?): FooterViewHolder {
                return FooterViewHolder(view, itemClickListener)
            }
        }
    }

    /**
     * ViewHolder for switching on type.
     */
    open class NotificationViewHolder
    /**
     * Constructor for the ViewHolder.
     * @param view              the inflated view
     * @param itemClickListener click listener for this view
     */
    constructor(view: View, itemClickListener: BaseViewHolder.ItemClickListener?) : BaseViewHolder(view, itemClickListener) {
        val notificationCard: DismissibleNotificationCard by bindView(R.id.adapter_dismissible_notification_card)
    }

    companion object {
        val TYPE_HEADER = 0
        val TYPE_LIST_ITEM = 1
        val TYPE_FOOTER = 2
    }

}
