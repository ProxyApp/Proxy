package com.shareyourproxy.api.rx.event;

import com.shareyourproxy.api.domain.model.Channel;

public class ChannelDialogRequestEvent {
    public final Channel channel;
    public ChannelDialogRequestEvent(Channel channel) {
        this.channel = channel;
    }
}
