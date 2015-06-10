package com.shareyourproxy.api.rx.event;

import com.shareyourproxy.api.domain.model.Channel;

/**
 * Channel selected event.
 */
public class SelectUserChannelEvent {
    public final Channel channel;

    public SelectUserChannelEvent(Channel channel) {
        this.channel = channel;
    }
}
