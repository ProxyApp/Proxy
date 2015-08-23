package com.shareyourproxy.api.rx.event;

import android.os.Parcel;

import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

/**
 * Created by Evan on 8/23/15.
 */
public class SyncAllUsersSuccessEvent extends EventCallback {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
