package com.shareyourproxy.api.rx.event;

import com.shareyourproxy.api.domain.model.Channel;

/**
 * Channel selected event.
 */
public class ChannelSelectedEvent {
    public final Channel channel;
    public ChannelSelectedEvent(Channel channel) {
       this.channel = channel;
    }
}
