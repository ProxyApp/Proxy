package com.shareyourproxy.api.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Used to signify if a {@link Channel} is in a {@link Group} or not.
 */
public class ChannelToggle implements Parcelable {
    public static final Creator<ChannelToggle> CREATOR =
        new Creator<ChannelToggle>() {
            @Override
            public ChannelToggle createFromParcel(Parcel in) {
                return new ChannelToggle(in);
            }

            @Override
            public ChannelToggle[] newArray(int size) {
                return new ChannelToggle[size];
            }
        };
    private final static java.lang.ClassLoader CL = ChannelToggle.class.getClassLoader();

    private Channel _channel;
    private boolean _inGroup;

    public ChannelToggle(Channel channel, boolean inGroup) {
        _channel = channel;
        _inGroup = inGroup;
    }

    private ChannelToggle(Parcel in) {
        this((Channel) in.readValue(CL), (boolean) in.readValue(CL));
    }

    public static ChannelToggle create(Channel channel, boolean inGroup) {
        return new ChannelToggle(channel, inGroup);
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
