package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 6/8/15.
 */
public class UserContactDeletedEventCallback extends UserEventCallback {
    private final static java.lang.ClassLoader CL =
        UserContactDeletedEventCallback.class.getClassLoader();
    public static final Creator<UserContactDeletedEventCallback> CREATOR =
        new Creator<UserContactDeletedEventCallback>() {
            @Override
            public UserContactDeletedEventCallback createFromParcel(Parcel in) {
                return new UserContactDeletedEventCallback(
                    (User) in.readValue(CL), (String) in.readValue(CL));
            }

            @Override
            public UserContactDeletedEventCallback[] newArray(int size) {
                return new UserContactDeletedEventCallback[size];
            }
        };
    private final String contactId;

    public UserContactDeletedEventCallback(User user, String contactId) {
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
