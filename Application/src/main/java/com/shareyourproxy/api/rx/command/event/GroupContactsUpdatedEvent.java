package com.shareyourproxy.api.rx.command.event;

    import android.os.Parcel;

    import com.shareyourproxy.api.domain.model.Contact;

/**
 * Created by Evan on 6/10/15.
 */
public class GroupContactsUpdatedEvent extends CommandEvent {

    public final boolean inGroup;
    public final Contact contact;

    public GroupContactsUpdatedEvent(Contact contact, boolean inGroup) {
        this.contact = contact;
        this.inGroup = inGroup;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(contact);
        dest.writeValue(inGroup);
    }
}
