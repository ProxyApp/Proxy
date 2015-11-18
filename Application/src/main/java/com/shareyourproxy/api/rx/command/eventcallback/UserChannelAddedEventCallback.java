package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;

/**
 * A newChannel was added or updated. If oldChannel is non-null, then this is an update.
 */
public class UserChannelAddedEventCallback extends UserEventCallback {
    private final static java.lang.ClassLoader CL = UserChannelAddedEventCallback.class.getClassLoader();
    public static final Creator<UserChannelAddedEventCallback> CREATOR =
        new Creator<UserChannelAddedEventCallback>() {
            @Override
            public UserChannelAddedEventCallback createFromParcel(Parcel in) {
                return new UserChannelAddedEventCallback(
                    (User) in.readValue(CL), (Channel) in.readValue(CL),
                    (Channel) in.readValue(CL));
            }

            @Override
            public UserChannelAddedEventCallback[] newArray(int size) {
                return new UserChannelAddedEventCallback[size];
            }
        };
    public final Channel newChannel;
    public final Channel oldChannel;

    public UserChannelAddedEventCallback(
        @NonNull User user, @Nullable Channel oldChannel, @NonNull Channel newChannel) {
        super(user);
        this.oldChannel = oldChannel;
        this.newChannel = newChannel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(oldChannel);
        dest.writeValue(newChannel);
    }
}
