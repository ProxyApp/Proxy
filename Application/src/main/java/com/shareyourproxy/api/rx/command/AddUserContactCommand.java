package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxUserContactSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

/**
 * Created by Evan on 6/8/15.
 */
public class AddUserContactCommand extends BaseCommand {
    public static final Creator<AddUserContactCommand> CREATOR = new Creator<AddUserContactCommand>
        () {
        @Override
        public AddUserContactCommand createFromParcel(Parcel in) {
            return new AddUserContactCommand(in);
        }

        @Override
        public AddUserContactCommand[] newArray(int size) {
            return new AddUserContactCommand[size];
        }
    };
    private final static java.lang.ClassLoader CL = AddUserContactCommand.class.getClassLoader();
    private final User user;
    private final Contact contact;

    public AddUserContactCommand(User user, Contact contact) {
        super(AddUserContactCommand.class.getPackage().getName(),
            AddUserContactCommand.class.getName());
        this.user = user;
        this.contact = contact;
    }

    private AddUserContactCommand(Parcel in) {
        this((User) in.readValue(CL), (Contact) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return RxUserContactSync.addUserContact(service, user, contact);
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
