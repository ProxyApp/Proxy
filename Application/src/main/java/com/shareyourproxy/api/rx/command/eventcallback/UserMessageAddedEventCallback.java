package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Message;

import java.util.HashMap;

/**
 * Created by Evan on 6/18/15.
 */
public class UserMessageAddedEventCallback extends EventCallback {
    private final static java.lang.ClassLoader CL =
        UserMessageAddedEventCallback.class.getClassLoader();
    public static final Creator<UserMessageAddedEventCallback> CREATOR =
        new Creator<UserMessageAddedEventCallback>() {
            @Override
            public UserMessageAddedEventCallback createFromParcel(Parcel in) {
                return new UserMessageAddedEventCallback(
                    (HashMap<String, Message>) in.readValue(CL));
            }

            @Override
            public UserMessageAddedEventCallback[] newArray(int size) {
                return new UserMessageAddedEventCallback[size];
            }
        };
    public final HashMap<String, Message> message;

    /**
     * Public constructor.
     *
     * @param message notification content
     */
    public UserMessageAddedEventCallback(@NonNull HashMap<String, Message> message) {
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
