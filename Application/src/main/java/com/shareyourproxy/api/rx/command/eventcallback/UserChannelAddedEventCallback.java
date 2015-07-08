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
