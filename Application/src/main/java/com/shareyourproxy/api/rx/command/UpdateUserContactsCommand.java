package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxUserContactSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Evan on 6/16/15.
 */
public class UpdateUserContactsCommand extends BaseCommand {
    public static final Parcelable.Creator<UpdateUserContactsCommand> CREATOR =
        new Parcelable.Creator<UpdateUserContactsCommand>() {
            @Override
            public UpdateUserContactsCommand createFromParcel(Parcel in) {
                return new UpdateUserContactsCommand(in);
            }

            @Override
            public UpdateUserContactsCommand[] newArray(int size) {
                return new UpdateUserContactsCommand[size];
            }
        };
    private final static java.lang.ClassLoader CL = UpdateUserContactsCommand.class
        .getClassLoader();
    public final User user;
    public final ArrayList<String> contacts;
    public final HashMap<String, Group> userGroups;

    public UpdateUserContactsCommand(
        @NonNull User user, @NonNull ArrayList<String> contacts,
        @NonNull HashMap<String, Group> userGroups) {
        super(UpdateUserContactsCommand.class.getPackage().getName(),
            UpdateUserContactsCommand.class.getName());
        this.user = user;
        this.contacts = contacts;
        this.userGroups = userGroups;
    }

    public UpdateUserContactsCommand(BaseCommand command) {
        super(UpdateUserContactsCommand.class.getPackage().getName(),
            UpdateUserContactsCommand.class.getName());
        this.user = ((UpdateUserContactsCommand) command).user;
        this.contacts = ((UpdateUserContactsCommand) command).contacts;
        this.userGroups = ((UpdateUserContactsCommand) command).userGroups;
    }

    private UpdateUserContactsCommand(Parcel in) {
        this((User) in.readValue(CL), (ArrayList<String>) in.readValue(CL),
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
