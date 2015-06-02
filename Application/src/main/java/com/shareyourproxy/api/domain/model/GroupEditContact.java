package com.shareyourproxy.api.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.shareyourproxy.app.dialog.UserGroupsDialog;

/**
 * Model for {@link UserGroupsDialog}.
 */
public class GroupEditContact implements Parcelable {
    public static final Creator<GroupEditContact> CREATOR = new Creator<GroupEditContact>
        () {
        @Override
        public GroupEditContact createFromParcel(Parcel in) {
            return new GroupEditContact(in);
        }

        @Override
        public GroupEditContact[] newArray(int size) {
            return new GroupEditContact[size];
        }
    };
    private final static java.lang.ClassLoader CL = GroupEditContact.class.getClassLoader();

    private Group _group;
    private boolean _hasContact;

    public GroupEditContact(Group group, boolean hasContact) {
        _group = group;
        _hasContact = hasContact;
    }

    private GroupEditContact(Parcel in) {
        this((Group) in.readValue(CL), (boolean) in.readValue(CL));
    }

    public static GroupEditContact create(Group group, boolean hasContact) {
        return new GroupEditContact(group, hasContact);
    }

    public Group getGroup() {
        return _group;
    }

    public void setGroup(Group group) {
        _group = group;
    }

    public boolean hasContact() {
        return _hasContact;
    }

    public void setHasContact(boolean hasContact) {
        _hasContact = hasContact;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(_group);
        dest.writeValue(_hasContact);
    }
}
