package com.shareyourproxy.api.rx.event;

import android.os.Parcel;

import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

/**
 * Sync all users command has successfully completed.
 */
public class SyncAllContactsSuccessEvent extends EventCallback {
    public static final Creator<SyncAllContactsSuccessEvent> CREATOR =
        new Creator<SyncAllContactsSuccessEvent>() {
            @Override
            public SyncAllContactsSuccessEvent createFromParcel(Parcel in) {
                return new SyncAllContactsSuccessEvent();
            }

            @Override
            public SyncAllContactsSuccessEvent[] newArray(int size) {
                return new SyncAllContactsSuccessEvent[size];
            }
        };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
