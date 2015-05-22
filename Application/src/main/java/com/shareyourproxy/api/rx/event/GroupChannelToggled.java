package com.shareyourproxy.api.rx.event;

import android.support.annotation.NonNull;

public class GroupChannelToggled {
    public final String channelid;

    public GroupChannelToggled(@NonNull String channelId) {
        this.channelid = channelId;
    }
}
