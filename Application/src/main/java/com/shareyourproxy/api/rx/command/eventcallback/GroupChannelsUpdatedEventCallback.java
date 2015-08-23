package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.model.User;

import java.util.HashMap;

/**
 * Group Name may have changed, channels updated.
 */
public class GroupChannelsUpdatedEventCallback extends UserEventCallback {
    public final Group group;
    public final HashMap<String, Id> channels;

    public GroupChannelsUpdatedEventCallback(
        User user, Group group, HashMap<String, Id> channels) {
        super(user);
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
