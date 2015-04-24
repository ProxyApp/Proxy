package com.proxy.event;

import com.proxy.api.domain.realm.RealmChannel;

/**
 * Channel selected event.
 */
public class ChannelSelectedEvent {
    public final RealmChannel channel;
    public ChannelSelectedEvent(RealmChannel channel) {
       this.channel = channel;
    }
}
