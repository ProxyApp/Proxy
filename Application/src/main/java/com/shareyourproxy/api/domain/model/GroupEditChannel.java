package com.shareyourproxy.api.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Used to signify if a {@link Channel} is in a {@link Group} or not.
 */
public class GroupEditChannel implements Parcelable {
    public static final Creator<GroupEditChannel> CREATOR =
        new Creator<GroupEditChannel>() {
            @Override
            public GroupEditChannel createFromParcel(Parcel in) {
                return new GroupEditChannel(in);
            }

            @Override
            public GroupEditChannel[] newArray(int size) {
                return new GroupEditChannel[size];
            }
        };
    private final static java.lang.ClassLoader CL = GroupEditChannel.class.getClassLoader();

    private Channel _channel;
    private boolean _inGroup;

    public GroupEditChannel(Channel channel, boolean inGroup) {
        _channel = channel;
        _inGroup = inGroup;
    }

    private GroupEditChannel(Parcel in) {
        this((Channel) in.readValue(CL), (boolean) in.readValue(CL));
    }

    public static GroupEditChannel create(Channel channel, boolean inGroup) {
        return new GroupEditChannel(channel, inGroup);
    }

    public Channel getChannel() {
        return _channel;
    }

    public void setChannel(Channel channel) {
        _channel = channel;
    }

    public boolean inGroup() {
        return _inGroup;
    }

    public void setInGroup(boolean inGroup) {
        _inGroup = inGroup;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(_channel);
        dest.writeValue(_inGroup);
    }
}
