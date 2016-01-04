package com.shareyourproxy.api.rx.event

import com.shareyourproxy.app.adapter.BaseRecyclerViewAdapter
import com.shareyourproxy.app.adapter.BaseViewHolder
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard

/**
 * Notification card was clicked.
 */
internal class NotificationCardActionEvent(private val adapter: BaseRecyclerViewAdapter, val holder: BaseViewHolder, val cardType: NotificationCard, val isHeader: Boolean)
