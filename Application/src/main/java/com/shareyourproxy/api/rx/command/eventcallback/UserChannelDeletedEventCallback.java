package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 5/21/15.
 */
public class UserChannelDeletedEventCallback extends UserEventCallback {
    public final Channel channel;

    public UserChannelDeletedEventCallback(@NonNull User user, @NonNull Channel channel) {
        super(user);
        this.channel = channel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(channel);
    }
}
