package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxUserContactSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Evan on 6/16/15.
 */
public class CheckUserContactsCommand extends BaseCommand {
    public static final Parcelable.Creator<CheckUserContactsCommand> CREATOR =
        new Parcelable.Creator<CheckUserContactsCommand>() {
            @Override
            public CheckUserContactsCommand createFromParcel(Parcel in) {
                return new CheckUserContactsCommand(in);
            }

            @Override
            public CheckUserContactsCommand[] newArray(int size) {
                return new CheckUserContactsCommand[size];
            }
        };
    private final static java.lang.ClassLoader CL = CheckUserContactsCommand.class.getClassLoader();
    public final User user;
    public final HashMap<String, Contact> contacts;
    public final HashMap<String, Group> userGroups;

    public CheckUserContactsCommand(
        @NonNull User user, @NonNull HashMap<String, Contact> contacts,
        @NonNull HashMap<String, Group> userGroups) {
        super(CheckUserContactsCommand.class.getPackage().getName(),
            CheckUserContactsCommand.class.getName());
        this.user = user;
        this.contacts = contacts;
        this.userGroups = userGroups;
    }

    public CheckUserContactsCommand(BaseCommand command) {
        super(CheckUserContactsCommand.class.getPackage().getName(),
            CheckUserContactsCommand.class.getName());
        this.user = ((CheckUserContactsCommand) command).user;
        this.contacts = ((CheckUserContactsCommand) command).contacts;
        this.userGroups = ((CheckUserContactsCommand) command).userGroups;
    }

    private CheckUserContactsCommand(Parcel in) {
        this((User) in.readValue(CL), (HashMap<String, Contact>) in.readValue(CL),
            (HashMap<String, Group>) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return RxUserContactSync.checkContacts(service, user, contacts, userGroups);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(contacts);
        dest.writeValue(userGroups);
    }
}
