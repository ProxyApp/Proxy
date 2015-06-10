package com.shareyourproxy.api.rx.command.callback;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 6/9/15.
 */
public class UserSavedEvent extends CommandEvent {
    public final User user;

    public UserSavedEvent(@NonNull User user){
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
