package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.User;

/**
 * EventCallbacks that have a user update.
 */
public abstract class UserEventCallback extends EventCallback implements Parcelable{
    public final User user;

    public UserEventCallback(@NonNull User user) {
        this.user = user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
    }
}
