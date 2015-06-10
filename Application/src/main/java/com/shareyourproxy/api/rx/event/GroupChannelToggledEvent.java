package com.shareyourproxy.api.rx.event;

import android.support.annotation.NonNull;

public class GroupChannelToggledEvent {
    public final String channelId;
    public GroupChannelToggledEvent(@NonNull String channelId) {
        this.channelId = channelId;
    }
}
