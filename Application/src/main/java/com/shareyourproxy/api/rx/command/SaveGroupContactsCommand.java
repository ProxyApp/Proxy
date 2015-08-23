package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;

import com.shareyourproxy.api.domain.model.GroupEditContact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxGroupContactSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evan on 6/8/15.
 */
public class SaveGroupContactsCommand extends BaseCommand {
    public static final Creator<SaveGroupContactsCommand> CREATOR =
        new Creator<SaveGroupContactsCommand>() {
            @Override
            public SaveGroupContactsCommand createFromParcel(Parcel in) {
                return new SaveGroupContactsCommand(in);
            }

            @Override
            public SaveGroupContactsCommand[] newArray(int size) {
                return new SaveGroupContactsCommand[size];
            }
        };
    private final static java.lang.ClassLoader CL =
        SaveGroupContactsCommand.class.getClassLoader();

    public final ArrayList<GroupEditContact> groups;
    public final String contactId;
    public final User user;

    public SaveGroupContactsCommand(
        User user, ArrayList<GroupEditContact> groups, String contactId) {
        super(SaveGroupContactsCommand.class.getPackage().getName(),
            SaveGroupContactsCommand.class.getName());
        this.user = user;
        this.groups = groups;
        this.contactId = contactId;
    }

    private SaveGroupContactsCommand(Parcel in) {
        this((User) in.readValue(CL), (ArrayList<GroupEditContact>) in.readValue(CL),
            (String) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return RxGroupContactSync
            .updateGroupContacts(service, user, groups, contactId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(groups);
        dest.writeValue(contactId);
    }

}
