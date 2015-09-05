package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
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

    public final ArrayList<GroupToggle> groups;
    public final String contactId;
    public final User user;

    public SaveGroupContactsCommand(
        @NonNull RxBusDriver rxBus, User user, ArrayList<GroupToggle> groups, String contactId) {
        super(SaveGroupContactsCommand.class.getPackage().getName(),
            SaveGroupContactsCommand.class.getName(), rxBus);
        this.user = user;
        this.groups = groups;
        this.contactId = contactId;
    }

    private SaveGroupContactsCommand(Parcel in) {
        this((RxBusDriver) in.readValue(CL), (User) in.readValue(CL),
            (ArrayList<GroupToggle>) in.readValue(CL), (String) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return RxGroupContactSync
            .updateGroupContacts(service, rxBus, user, groups, contactId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeValue(user);
        dest.writeValue(groups);
        dest.writeValue(contactId);
    }

}
