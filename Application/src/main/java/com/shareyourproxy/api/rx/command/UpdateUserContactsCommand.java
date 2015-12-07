package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.shareyourproxy.api.rx.RxUserContactSync.checkContacts;

/**
 * Update a user's contacts.
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

    public UpdateUserContactsCommand(@NonNull User user, @NonNull ArrayList<String> contacts,
        @NonNull HashMap<String, Group> userGroups) {
        this.user = user;
        this.contacts = contacts;
        this.userGroups = userGroups;
    }

    private UpdateUserContactsCommand(Parcel in) {
        this((User) in.readValue(CL), (ArrayList<String>) in.readValue(CL),
            (HashMap<String, Group>) in.readValue(CL));
    }

    @Override
    public EventCallback execute(Service service) {
        return checkContacts(service, user, contacts, userGroups);
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
