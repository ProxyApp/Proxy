package com.proxy.api.rx.event;

import com.proxy.api.domain.model.Channel;

public class ChannelDialogRequestEvent {
    public final Channel channel;
    public ChannelDialogRequestEvent(Channel channel) {
        this.channel = channel;
    }
}
