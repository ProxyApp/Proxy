package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;

/**
 * User group channel updated.
 */
public class UserGroupAddedEventCallback extends UserEventCallback {
    private final static java.lang.ClassLoader CL = UserGroupAddedEventCallback.class.getClassLoader();
    public static final Creator<UserGroupAddedEventCallback> CREATOR =
        new Creator<UserGroupAddedEventCallback>() {
            @Override
            public UserGroupAddedEventCallback createFromParcel(Parcel in) {
                return new UserGroupAddedEventCallback(
                    (User) in.readValue(CL), (Group) in.readValue(CL));
            }

            @Override
            public UserGroupAddedEventCallback[] newArray(int size) {
                return new UserGroupAddedEventCallback[size];
            }
        };
    public final Group group;


    /**
     * Public constructor.
     *
     * @param group this events group
     */
    public UserGroupAddedEventCallback(User user, Group group) {
        super(user);
        this.group = group;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(group);
    }
}
