package com.shareyourproxy.api.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.shareyourproxy.app.dialog.UserGroupsDialog;

/**
 * Model for {@link UserGroupsDialog}.
 */
public class GroupToggle implements Parcelable {
    public static final Creator<GroupToggle> CREATOR = new Creator<GroupToggle>
        () {
        @Override
        public GroupToggle createFromParcel(Parcel in) {
            return new GroupToggle(in);
        }

        @Override
        public GroupToggle[] newArray(int size) {
            return new GroupToggle[size];
        }
    };
    private final static java.lang.ClassLoader CL = GroupToggle.class.getClassLoader();

    private Group _group;
    private boolean _isChecked;

    public GroupToggle(Group group, boolean isChecked) {
        _group = group;
        _isChecked = isChecked;
    }

    private GroupToggle(Parcel in) {
        this((Group) in.readValue(CL), (boolean) in.readValue(CL));
    }

    public static GroupToggle create(Group group, boolean hasContact) {
        return new GroupToggle(group, hasContact);
    }

    public Group getGroup() {
        return _group;
    }

    public void setGroup(Group group) {
        _group = group;
    }

    public boolean isChecked() {
        return _isChecked;
    }

    public void setHasContact(boolean hasContact) {
        _isChecked = hasContact;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(_group);
        dest.writeValue(_isChecked);
    }
}
