package com.shareyourproxy.api.rx.command;

import android.app.IntentService;
import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.GroupEditContact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxGroupContactSync;
import com.shareyourproxy.api.rx.command.event.CommandEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evan on 6/8/15.
 */
public class UpdateGroupContactsCommand extends BaseCommand {
    public static final Creator<UpdateGroupContactsCommand> CREATOR =
        new Creator<UpdateGroupContactsCommand>() {
            @Override
            public UpdateGroupContactsCommand createFromParcel(Parcel in) {
                return new UpdateGroupContactsCommand(in);
            }

            @Override
            public UpdateGroupContactsCommand[] newArray(int size) {
                return new UpdateGroupContactsCommand[size];
            }
        };
    private final static java.lang.ClassLoader CL = UpdateGroupContactsCommand.class
        .getClassLoader();
    public final ArrayList<GroupEditContact> groups;
    public final Contact contact;
    public final User user;

    public UpdateGroupContactsCommand(
        User user, ArrayList<GroupEditContact> groups, Contact contact) {
        super(UpdateGroupContactsCommand.class.getPackage().getName(),
            UpdateGroupContactsCommand.class.getName());
        this.user = user;
        this.groups = groups;
        this.contact = contact;
    }

    public UpdateGroupContactsCommand(BaseCommand command) {
        super(UpdateGroupContactsCommand.class.getPackage().getName(),
            UpdateGroupContactsCommand.class.getName());
        this.user = ((UpdateGroupContactsCommand) command).user;
        this.groups = ((UpdateGroupContactsCommand) command).groups;
        this.contact = ((UpdateGroupContactsCommand) command).contact;
    }

    private UpdateGroupContactsCommand(Parcel in) {
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
