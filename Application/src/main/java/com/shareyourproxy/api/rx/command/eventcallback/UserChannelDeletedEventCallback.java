package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 5/21/15.
 */
public class UserChannelDeletedEventCallback extends UserEventCallback {
    private final static java.lang.ClassLoader CL =
        UserChannelDeletedEventCallback.class.getClassLoader();

    public static final Creator<UserChannelDeletedEventCallback> CREATOR =
        new Creator<UserChannelDeletedEventCallback>() {
            @Override
            public UserChannelDeletedEventCallback createFromParcel(Parcel in) {
                return new UserChannelDeletedEventCallback(
                    (User) in.readValue(CL), (Channel) in.readValue(CL), (int) in.readValue(CL));
            }

            @Override
            public UserChannelDeletedEventCallback[] newArray(int size) {
                return new UserChannelDeletedEventCallback[size];
            }
        };
    public final Channel channel;
    public final int position;

    public UserChannelDeletedEventCallback(
        @NonNull User user, @NonNull Channel channel, int position) {
        super(user);
        this.channel = channel;
        this.position = position;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(channel);
        dest.writeValue(position);
    }
}
