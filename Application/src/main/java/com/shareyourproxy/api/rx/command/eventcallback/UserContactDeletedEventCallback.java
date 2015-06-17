package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 6/8/15.
 */
public class UserContactDeletedEventCallback extends UserEventCallback {
    private final Contact contact;

    public UserContactDeletedEventCallback(User user, Contact contact) {
        super(user);
        this.contact = contact;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(contact);
    }
}
