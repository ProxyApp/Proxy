package com.shareyourproxy.api.rx.command.eventcallback;

import android.app.Notification;
import android.os.Parcel;

import java.util.ArrayList;

/**
 * Created by Evan on 6/18/15.
 */
public class UserMessagesDownloadedEventCallback extends EventCallback {
    private final static java.lang.ClassLoader CL =
        UserMessagesDownloadedEventCallback.class.getClassLoader();
    public static final Creator<UserMessagesDownloadedEventCallback> CREATOR =
        new Creator<UserMessagesDownloadedEventCallback>() {
            @Override
            public UserMessagesDownloadedEventCallback createFromParcel(Parcel in) {
                return new UserMessagesDownloadedEventCallback(
                    (ArrayList<Notification>) in.readValue(CL));
            }

            @Override
            public UserMessagesDownloadedEventCallback[] newArray(int size) {
                return new UserMessagesDownloadedEventCallback[size];
            }
        };
    public final ArrayList<Notification> notifications;

    public UserMessagesDownloadedEventCallback(ArrayList<Notification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(notifications);
    }
}
