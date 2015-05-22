package com.proxy.api.rx.event;

import com.proxy.api.domain.model.Channel;

/**
 * Created by Evan on 5/21/15.
 */
public class DeleteChannelEvent {
    public final Channel channel;
    public DeleteChannelEvent(Channel channel) {
        this.channel = channel;
    }
}
