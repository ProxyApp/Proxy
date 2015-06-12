package com.shareyourproxy.api.rx.event;

import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.GroupEditChannel;

public class GroupChannelToggledEvent {
    public final GroupEditChannel editChannel;

    public GroupChannelToggledEvent(@NonNull GroupEditChannel editChannel) {
        this.editChannel = editChannel;
    }
}
