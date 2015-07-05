package com.shareyourproxy.api.rx.command.eventcallback;

import android.app.Notification;
import android.os.Parcel;

import java.util.ArrayList;

/**
 * Created by Evan on 6/18/15.
 */
public class UserMessagesDownloadedEventCallback extends EventCallback {
    public final ArrayList<Notification> notifications;

    public UserMessagesDownloadedEventCallback(ArrayList<Notification> notifications) {
        this.notifications = notifications;
    }

    public UserMessagesDownloadedEventCallback() {
        this.notifications = new ArrayList<>();
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
