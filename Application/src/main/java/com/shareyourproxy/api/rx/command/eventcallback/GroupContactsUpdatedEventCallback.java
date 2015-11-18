package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;

import java.util.List;

/**
 * Created by Evan on 6/10/15.
 */
public class GroupContactsUpdatedEventCallback extends UserEventCallback {
    private final static java.lang.ClassLoader CL =
        GroupContactsUpdatedEventCallback.class.getClassLoader();
    public static final Creator<GroupContactsUpdatedEventCallback> CREATOR =
        new Creator<GroupContactsUpdatedEventCallback>() {
            @Override
            public GroupContactsUpdatedEventCallback createFromParcel(Parcel in) {
                return new GroupContactsUpdatedEventCallback(
                    (User) in.readValue(CL), (String) in.readValue(CL),
                    (List<Group>) in.readValue(CL));
            }

            @Override
            public GroupContactsUpdatedEventCallback[] newArray(int size) {
                return new GroupContactsUpdatedEventCallback[size];
            }
        };
    public final List<Group> contactGroups;
    public final String contactId;

    public GroupContactsUpdatedEventCallback(User user, String contactId, List<Group> groups) {
        super(user);
        this.contactId = contactId;
        this.contactGroups = groups;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(contactId);
        dest.writeValue(contactGroups);
    }
}
