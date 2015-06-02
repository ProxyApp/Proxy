package com.shareyourproxy.api.rx.command.event;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 6/8/15.
 */
public class UserContactDeletedEvent extends CommandEvent {
    private final User user;
    private final Contact contact;

    public UserContactDeletedEvent(User user, Contact contact) {
        this.user = user;
        this.contact = contact;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
        dest.writeParcelable(contact, flags);
    }
}
