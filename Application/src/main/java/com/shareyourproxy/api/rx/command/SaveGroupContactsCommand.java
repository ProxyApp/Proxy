package com.shareyourproxy.api.rx.command;

import android.app.IntentService;
import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.GroupEditContact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxGroupContactSync;
import com.shareyourproxy.api.rx.command.callback.CommandEvent;

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
    public final Contact contact;
    public final User user;

    public SaveGroupContactsCommand(
        User user, ArrayList<GroupEditContact> groups, Contact contact) {
        super(SaveGroupContactsCommand.class.getPackage().getName(),
            SaveGroupContactsCommand.class.getName());
        this.user = user;
        this.groups = groups;
        this.contact = contact;
    }

    public SaveGroupContactsCommand(BaseCommand command) {
        super(SaveGroupContactsCommand.class.getPackage().getName(),
            SaveGroupContactsCommand.class.getName());
        this.user = ((SaveGroupContactsCommand) command).user;
        this.groups = ((SaveGroupContactsCommand) command).groups;
        this.contact = ((SaveGroupContactsCommand) command).contact;
    }

    private SaveGroupContactsCommand(Parcel in) {
        this((User) in.readValue(CL), (ArrayList<GroupEditContact>) in.readValue(CL),
            (Contact) in.readValue(CL));
    }

    @Override
    public List<CommandEvent> execute(IntentService service) {
        return RxGroupContactSync
            .updateGroupContacts(service, user, groups, contact);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(groups);
        dest.writeValue(contact);
    }

}
