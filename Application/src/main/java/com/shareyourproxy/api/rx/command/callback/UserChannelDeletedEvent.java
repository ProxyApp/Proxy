package com.shareyourproxy.api.rx.command.callback;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 5/21/15.
 */
public class UserChannelDeletedEvent extends CommandEvent {
    public final Channel channel;
    public final User user;

    public UserChannelDeletedEvent(@NonNull User user, @NonNull Channel channel) {
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
