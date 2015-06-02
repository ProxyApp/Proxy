package com.shareyourproxy.api.rx.event;

import com.shareyourproxy.api.domain.model.Channel;

/**
 * Created by Evan on 5/5/15.
 */
public class ChannelAddedEvent {
    public final Channel channel;
    public ChannelAddedEvent(Channel channel) {
        this.channel = channel;
    }
}
