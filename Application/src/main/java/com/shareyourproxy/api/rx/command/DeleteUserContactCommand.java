package com.shareyourproxy.api.rx.command;

import android.app.IntentService;
import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxUserContactSync;
import com.shareyourproxy.api.rx.command.callback.CommandEvent;

import java.util.List;

/**
 * Created by Evan on 6/8/15.
 */
public class DeleteUserContactCommand extends BaseCommand {
    public static final Creator<DeleteUserContactCommand> CREATOR =
        new Creator<DeleteUserContactCommand>() {
        @Override
        public DeleteUserContactCommand createFromParcel(Parcel in) {
            return new DeleteUserContactCommand(in);
        }

        @Override
        public DeleteUserContactCommand[] newArray(int size) {
            return new DeleteUserContactCommand[size];
        }
    };
    private final static java.lang.ClassLoader CL = DeleteUserContactCommand.class.getClassLoader();
    private final User user;
    private final Contact contact;

    public DeleteUserContactCommand(User user, Contact contact) {
        super(DeleteUserContactCommand.class.getPackage().getName(),
            DeleteUserContactCommand.class.getName());
        this.user = user;
        this.contact = contact;
    }

    public DeleteUserContactCommand(BaseCommand command) {
        super(DeleteUserContactCommand.class.getPackage().getName(),
            DeleteUserContactCommand.class.getName());
        this.user = ((DeleteUserContactCommand) command).user;
        this.contact = ((DeleteUserContactCommand) command).contact;
    }

    private DeleteUserContactCommand(Parcel in) {
        this((User) in.readValue(CL), (Contact) in.readValue(CL));
    }

    @Override
    public List<CommandEvent> execute(IntentService service) {
        return RxUserContactSync.deleteUserContact(service, user, contact);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(contact);
    }
}
