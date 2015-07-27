package com.shareyourproxy.api.rx.event;

import android.os.Parcel;

import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

/**
 * A ShareLink message has been generated.
 */
public class ShareLinkEvent extends EventCallback {
    public final String message;

    public ShareLinkEvent(String message) {
        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(message);
    }
}
