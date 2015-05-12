package com.proxy.event;

import com.proxy.api.domain.realm.RealmChannel;

public class ChannelDialogRequestEvent {
    public final RealmChannel channel;
    public ChannelDialogRequestEvent(RealmChannel channel) {
        this.channel = channel;
    }
}
