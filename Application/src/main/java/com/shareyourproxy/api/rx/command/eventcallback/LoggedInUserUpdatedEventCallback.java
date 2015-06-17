package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 6/9/15.
 */
public class LoggedInUserUpdatedEventCallback extends UserEventCallback {

    public LoggedInUserUpdatedEventCallback(@NonNull User user) {
        super(user);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
