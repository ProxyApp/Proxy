package com.shareyourproxy.api.rx.event;

import com.shareyourproxy.app.adapter.BaseRecyclerViewAdapter;
import com.shareyourproxy.app.adapter.BaseViewHolder;
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard;

/**
 * Created by Evan on 11/10/15.
 */
public class NotificationCardDismissEvent {
    public final NotificationCard cardType;
    public final boolean isHeader;
    public final BaseViewHolder holder;
    public final BaseRecyclerViewAdapter adapter;

    public NotificationCardDismissEvent(
        BaseRecyclerViewAdapter adapter, BaseViewHolder holder, NotificationCard cardType,
        boolean isHeader) {
        this.holder = holder;
        this.adapter = adapter;
        this.cardType = cardType;
        this.isHeader = isHeader;
    }
}
