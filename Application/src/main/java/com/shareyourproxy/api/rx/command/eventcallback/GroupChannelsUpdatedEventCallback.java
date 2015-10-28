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
    private final static java.lang.ClassLoader CL =
        GroupChannelsUpdatedEventCallback.class.getClassLoader();
    public static final Creator<GroupChannelsUpdatedEventCallback> CREATOR =
        new Creator<GroupChannelsUpdatedEventCallback>() {
            @Override
            public GroupChannelsUpdatedEventCallback createFromParcel(Parcel in) {
                return new GroupChannelsUpdatedEventCallback(
                    (User) in.readValue(CL), (Group) in.readValue(CL),
                    (HashSet<String>) in.readValue(CL),(GroupEditType) in.readValue(CL));
            }

            @Override
            public GroupChannelsUpdatedEventCallback[] newArray(int size) {
                return new GroupChannelsUpdatedEventCallback[size];
            }
        };
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
