package com.shareyourproxy.api.rx.event;

import com.shareyourproxy.app.adapter.BaseRecyclerViewAdapter;
import com.shareyourproxy.app.adapter.BaseViewHolder;
import com.shareyourproxy.widget.DismissibleNotificationCard.NotificationCard;

/**
 * Created by Evan on 11/10/15.
 */
public class NotificationCardActionEvent {
    public final NotificationCard cardType;
    public final boolean isHeader;
    public final BaseViewHolder holder;
    private final BaseRecyclerViewAdapter adapter;

    public NotificationCardActionEvent(
        BaseRecyclerViewAdapter adapter, BaseViewHolder holder, NotificationCard cardType,
        boolean isHeader) {
        this.adapter = adapter;
        this.holder = holder;
        this.cardType = cardType;
        this.isHeader = isHeader;
    }
}
