package com.shareyourproxy.api.rx.event;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 11/9/15.
 */
public class ChannelAddedEvent {

    public final Channel channel;
    public final User user;

    public ChannelAddedEvent(User user, Channel channel) {
        this.channel = channel;
        this.user = user;
    }
}
