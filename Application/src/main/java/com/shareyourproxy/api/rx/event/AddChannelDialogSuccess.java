package com.shareyourproxy.api.rx.event;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 11/17/15.
 */
public class AddChannelDialogSuccess {
    public final User user;
    public final Channel channel;

    public AddChannelDialogSuccess(User user, Channel channel) {
        this.user = user;
        this.channel = channel;
    }
}
