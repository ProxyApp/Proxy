package com.shareyourproxy.api.rx.command.callback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;

/**
 * Created by Evan on 6/8/15.
 */
public class GroupContactDeletedEvent extends CommandEvent {
    public final Group group;
    public final Contact contact;

    public GroupContactDeletedEvent(Group group, Contact contact) {
        this.group = group;
        this.contact = contact;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(group, flags);
        dest.writeParcelable(contact, flags);
    }
}
