package com.shareyourproxy.api.rx.command.callback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Group;

import java.util.ArrayList;

/**
 * Group Name may have changed, channels updated.
 */
public class GroupChannelsUpdatedEvent extends CommandEvent {
    public final Group group;
    public final ArrayList<Channel> channels;

    public GroupChannelsUpdatedEvent(Group group, ArrayList<Channel> channels) {
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
