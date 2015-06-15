package com.shareyourproxy.api.rx.command;

import android.app.IntentService;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxUserContactSync;
import com.shareyourproxy.api.rx.command.callback.CommandEvent;

import java.util.ArrayList;
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
    public final ArrayList<Contact> contacts;
    public final ArrayList<Group> userGroups;

    public CheckUserContactsCommand(
        @NonNull User user, @NonNull ArrayList<Contact> contacts,
        @NonNull ArrayList<Group> userGroups) {
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
        this((User) in.readValue(CL), (ArrayList<Contact>) in.readValue(CL),
            (ArrayList<Group>) in.readValue(CL));
    }

    @Override
    public List<CommandEvent> execute(IntentService service) {
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
