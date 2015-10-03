package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType;

import java.util.HashSet;

/**
 * Group Name may have changed, channels updated.
 */
public class GroupChannelsUpdatedEventCallback extends UserEventCallback {
    public final Group group;
    public final HashSet<String> channels;
    public final GroupEditType groupEditType;

    public GroupChannelsUpdatedEventCallback(
        User user, Group group, HashSet<String> channels, GroupEditType groupEditType) {
        super(user);
        this.group = group;
        this.channels = channels;
        this.groupEditType = groupEditType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(group);
        dest.writeValue(channels);
        dest.writeValue(groupEditType);
    }
}
