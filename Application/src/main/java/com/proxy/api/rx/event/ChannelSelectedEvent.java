package com.proxy.api.rx.event;

import com.proxy.api.domain.model.Channel;

/**
 * Channel selected event.
 */
public class ChannelSelectedEvent {
    public final Channel channel;
    public ChannelSelectedEvent(Channel channel) {
       this.channel = channel;
    }
}
