package com.shareyourproxy.api.rx.command.event;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;

import java.util.ArrayList;

/**
 * Created by Evan on 6/10/15.
 */
public class GroupContactsUpdatedEvent extends CommandEvent {

    public final ArrayList<Group> contactGroups;
    public final Contact contact;

    public GroupContactsUpdatedEvent(Contact contact, ArrayList<Group> groups) {
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
