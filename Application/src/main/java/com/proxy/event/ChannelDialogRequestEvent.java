package com.proxy.event;

import com.proxy.api.domain.realm.RealmChannel;

/**
 * Created by Evan on 5/5/15.
 */
public class ChannelDialogRequestEvent {
    public final RealmChannel channel;
    public ChannelDialogRequestEvent(RealmChannel channel) {
        this.channel = channel;
    }
}
