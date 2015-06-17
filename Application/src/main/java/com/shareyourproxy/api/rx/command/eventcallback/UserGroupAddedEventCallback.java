package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 6/8/15.
 */
public class UserGroupAddedEventCallback extends UserEventCallback {
    public final Group group;

    /**
     * Public constructor.
     *
     * @param group this events group
     */
    public UserGroupAddedEventCallback(@NonNull User user, @NonNull Group group) {
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
