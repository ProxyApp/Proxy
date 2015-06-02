package com.shareyourproxy.api.rx.command.event;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 5/5/15.
 */
public class UserChannelAddedEvent extends CommandEvent {
    public final User user;
    public final Channel channel;

    public UserChannelAddedEvent(@NonNull User user, @NonNull Channel channel) {
        this.user = user;
        this.channel = channel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
        dest.writeParcelable(channel, flags);
    }
}
