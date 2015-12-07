package com.shareyourproxy.api.rx.event;

import android.os.Parcel;

import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

/**
 * A ShareLink message has been generated.
 */
public class ShareLinkEventCallback extends EventCallback {
    private final static java.lang.ClassLoader CL =
        ShareLinkEventCallback.class.getClassLoader();
    public static final Creator<ShareLinkEventCallback> CREATOR =
        new Creator<ShareLinkEventCallback>() {
            @Override
            public ShareLinkEventCallback createFromParcel(Parcel in) {
                return new ShareLinkEventCallback((String) in.readValue(CL));
            }

            @Override
            public ShareLinkEventCallback[] newArray(int size) {
                return new ShareLinkEventCallback[size];
            }
        };
    public final String message;

    public ShareLinkEventCallback(String message) {
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
