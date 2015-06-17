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
        @NonNull User user, @NonNull Channel newChannel, @Nullable Channel oldChannel) {
        super(user);
        this.newChannel = newChannel;
        this.oldChannel = oldChannel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(newChannel);
        dest.writeValue(oldChannel);
    }
}
