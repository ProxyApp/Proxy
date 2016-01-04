package com.shareyourproxy.api.rx.event

import com.shareyourproxy.app.adapter.BaseRecyclerViewAdapter
import com.shareyourproxy.app.adapter.BaseViewHolder
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard

/**
 * Created by Evan on 11/10/15.
 */
internal class NotificationCardDismissEvent(val adapter: BaseRecyclerViewAdapter, val holder: BaseViewHolder, val cardType: NotificationCard, val isHeader: Boolean)
