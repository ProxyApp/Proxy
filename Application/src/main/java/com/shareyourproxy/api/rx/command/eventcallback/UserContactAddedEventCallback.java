package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 6/8/15.
 */
public class UserContactAddedEventCallback extends UserEventCallback {
    private final static java.lang.ClassLoader CL =
        UserContactAddedEventCallback.class.getClassLoader();
    public static final Creator<UserContactAddedEventCallback> CREATOR =
        new Creator<UserContactAddedEventCallback>() {
            @Override
            public UserContactAddedEventCallback createFromParcel(Parcel in) {
                return new UserContactAddedEventCallback(
                    (User) in.readValue(CL),(String) in.readValue(CL));
            }

            @Override
            public UserContactAddedEventCallback[] newArray(int size) {
                return new UserContactAddedEventCallback[size];
            }
        };
    public final String contactId;

    public UserContactAddedEventCallback(User user, String contactId) {
        super(user);
        this.contactId = contactId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(contactId);
    }
}
