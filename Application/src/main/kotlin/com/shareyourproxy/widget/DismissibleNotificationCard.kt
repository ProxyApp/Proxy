package com.shareyourproxy.widget

import android.content.Context
import android.support.v4.content.ContextCompat.getColor
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.shareyourproxy.Constants.KEY_DISMISSED_CUSTOM_URL
import com.shareyourproxy.Constants.KEY_DISMISSED_INVITE_FRIENDS
import com.shareyourproxy.Constants.KEY_DISMISSED_MAIN_GROUP
import com.shareyourproxy.Constants.KEY_DISMISSED_PUBLIC_GROUP
import com.shareyourproxy.Constants.KEY_DISMISSED_SAFE_INFO
import com.shareyourproxy.Constants.KEY_DISMISSED_SHARE_PROFILE
import com.shareyourproxy.Constants.KEY_DISMISSED_WHOOPS
import com.shareyourproxy.R
import com.shareyourproxy.R.color.*
import com.shareyourproxy.R.dimen.common_svg_null_screen_mini
import com.shareyourproxy.R.id.*
import com.shareyourproxy.R.raw.*
import com.shareyourproxy.R.string.*
import com.shareyourproxy.R.styleable.*
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.event.NotificationCardActionEvent
import com.shareyourproxy.api.rx.event.NotificationCardDismissEvent
import com.shareyourproxy.app.adapter.BaseRecyclerViewAdapter
import com.shareyourproxy.app.adapter.BaseViewHolder
import com.shareyourproxy.app.adapter.NotificationRecyclerAdapter.HeaderViewHolder
import com.shareyourproxy.util.ButterKnife.bindDimen
import com.shareyourproxy.util.ButterKnife.bindView
import com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard.*

/**
 * Dismissable Notification card for recycler view headers.
 */
internal final class DismissibleNotificationCard : FrameLayout {
    private val container: RelativeLayout by bindView(widget_notification_container)
    private val title: TextView by bindView(widget_notification_content_title)
    private val message: TextView by bindView(widget_notification_content_message)
    private val imageView: ImageView by bindView(widget_notification_imageview)
    private val dismissTextView: TextView by bindView(widget_notification_dismiss_text)
    private val actionTextView: TextView by bindView(widget_notification_action_text)
    private val dimenSvgNullSmall: Int by bindDimen(common_svg_null_screen_mini);
    private val onClickDismiss: View.OnClickListener = android.view.View.OnClickListener {
        visibility = GONE
        when (notificationCard) {
            SAFE_INFO,
            SHARE_PROFILE,
            INVITE_FRIENDS,
            CUSTOM_URL,
            PUBLIC_GROUPS,
            MAIN_GROUPS -> post(NotificationCardDismissEvent(adapter!!, holder!!, notificationCard, isHeaderOrFooter))
            else -> {
            }
        }
    }
    private val onClickAction: View.OnClickListener = android.view.View.OnClickListener {
        when (notificationCard) {
            SAFE_INFO,
            SHARE_PROFILE,
            INVITE_FRIENDS,
            CUSTOM_URL,
            PUBLIC_GROUPS,
            MAIN_GROUPS -> post(NotificationCardActionEvent(adapter!!, holder!!, notificationCard, isHeaderOrFooter))
            else -> {
            }
        }
    }
    private val isHeaderOrFooter: Boolean get() = holder is HeaderViewHolder
    private var notificationCard = WHOOPS
    private var showDismiss = false
    private var showAction = false
    private var holder: BaseViewHolder? = null
    private var adapter: BaseRecyclerViewAdapter? = null

    constructor(context: Context) : super(context) {
        initializeView(context)
        initializeCardView(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initializeView(context)
        initializeAttributeValues(context, attrs)
        initializeCardView(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializeView(context)
        initializeAttributeValues(context, attrs)
        initializeCardView(context)
    }

    internal fun createNotificationCard(adapter: BaseRecyclerViewAdapter, holder: BaseViewHolder, notificationCard: NotificationCard, showDismiss: Boolean, showAction: Boolean) {
        this.adapter = adapter
        this.holder = holder
        this.notificationCard = notificationCard
        this.showDismiss = showDismiss
        this.showAction = showAction
        initializeCardView(holder.view.context)
        invalidate()
    }

    private fun initializeAttributeValues(context: Context, attrs: AttributeSet) {
        // Initialize view params
        val a = context.theme.obtainStyledAttributes(attrs, DismissibleNotificationCard, 0, 0)
        try {
            showDismiss = a.getBoolean(DismissibleNotificationCard_showDismiss, false)
            showAction = a.getBoolean(DismissibleNotificationCard_showAction, false)
            notificationCard = NotificationCard[a.getInteger(DismissibleNotificationCard_notification, 0)]
        } finally {
            a.recycle()
        }
    }

    private fun initializeView(context: Context) {
        inflate(context, R.layout.notification_card, this)
        dismissTextView.setOnClickListener(onClickDismiss)
        actionTextView.setOnClickListener(onClickAction)
    }

    private fun initializeCardView(context: Context) {
        initializeContent(context)
        dismissTextView.visibility = if (showDismiss) VISIBLE else GONE
        actionTextView.visibility = if (showAction) setVisible(context) else GONE
        container.setBackgroundColor(getColor(context, notificationCard.colorRes))
    }

    /**
     * Set the message text if we're going to show this view
     */
    fun setVisible(context: Context): Int {
        actionTextView.text = context.getString(notificationCard.actionRes)
        return VISIBLE
    }

    private fun initializeContent(context: Context) {
        val title = context.getString(notificationCard.titleRes)
        val message = context.getString(notificationCard.messageRes)
        this.title.text = title
        this.message.text = message

        // Set custom drawable
        imageView.setImageDrawable(svgToBitmapDrawable(context, notificationCard.iconRes, dimenSvgNullSmall))
    }

    enum class NotificationCard private constructor(val value: Int, val key: String, val titleRes: Int, val messageRes: Int, val iconRes: Int, val colorRes: Int, val actionRes: Int) {
        WHOOPS(0, KEY_DISMISSED_WHOOPS, notification_safe_info_title, notification_safe_info_message, ic_alien, common_deep_purple, ok),
        SAFE_INFO(1, KEY_DISMISSED_SAFE_INFO, notification_safe_info_title, notification_safe_info_message, ic_chameleon_framed, common_proxy_purple, ok),
        SHARE_PROFILE(2, KEY_DISMISSED_SHARE_PROFILE, notification_share_profile_title, notification_share_profile_message, ic_carroll_share, common_blue, ok),
        CUSTOM_URL(3, KEY_DISMISSED_CUSTOM_URL, notification_custom_url_title, notification_custom_url_message, ic_sexbot_custom, common_blue, ok),
        INVITE_FRIENDS(4, KEY_DISMISSED_INVITE_FRIENDS, notification_invite_friends_title, notification_invite_friends_message, ic_jamal, common_light_blue, send_invite),
        PUBLIC_GROUPS(5, KEY_DISMISSED_PUBLIC_GROUP, notification_public_groups_title, notification_public_groups_message, ic_chameleon_framed, common_proxy_purple, ok),
        MAIN_GROUPS(6, KEY_DISMISSED_MAIN_GROUP, notification_main_groups_title, notification_main_groups_message, ic_owl, common_proxy_orange, ok);

        companion object {
            operator fun get(intValue: Int): NotificationCard {
                NotificationCard.values().forEach {
                    if (it.value == intValue) {
                        return it
                    }
                }
                return WHOOPS
            }
        }
    }
}
