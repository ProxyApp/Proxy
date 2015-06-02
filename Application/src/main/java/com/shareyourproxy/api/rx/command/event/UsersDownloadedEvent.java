package com.shareyourproxy.api.rx.command.event;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.User;

import java.util.ArrayList;

/**
 * Created by Evan on 6/9/15.
 */
public class UsersDownloadedEvent extends CommandEvent {
    public final ArrayList<User> users;

    public UsersDownloadedEvent(ArrayList<User> users) {
        this.users = users;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(users);
    }
}
