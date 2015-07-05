package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Group;

import java.util.HashMap;

/**
 * Group Name may have changed, channels updated.
 */
public class GroupChannelsUpdatedEventCallback extends EventCallback {
    public final Group group;
    public final HashMap<String, Channel> channels;

    public GroupChannelsUpdatedEventCallback(Group group, HashMap<String, Channel> channels) {
        this.group = group;
        this.channels = channels;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(group);
        dest.writeValue(channels);
    }
}
