package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.User;

import java.util.HashMap;

/**
 * Created by Evan on 6/9/15.
 */
public class UsersDownloadedEventCallback extends EventCallback {
    public final HashMap<String, User> users;

    public UsersDownloadedEventCallback(HashMap<String, User> users) {
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
