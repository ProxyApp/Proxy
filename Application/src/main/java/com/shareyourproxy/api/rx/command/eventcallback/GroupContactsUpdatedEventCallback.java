package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;

import java.util.List;

/**
 * Created by Evan on 6/10/15.
 */
public class GroupContactsUpdatedEventCallback extends UserEventCallback {

    public final List<Group> contactGroups;
    public final Contact contact;

    public GroupContactsUpdatedEventCallback(User user, Contact contact, List<Group> groups) {
        super(user);
        this.contact = contact;
        this.contactGroups = groups;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(contact);
        dest.writeValue(contactGroups);
    }
}
