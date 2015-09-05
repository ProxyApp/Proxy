package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

import static com.shareyourproxy.api.rx.RxUserContactSync.addUserContact;

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
    private final String contactId;

    public AddUserContactCommand(
        @NonNull RxBusDriver rxBus, User user, String contactId) {
        super(AddUserContactCommand.class.getPackage().getName(),
            AddUserContactCommand.class.getName(), rxBus);
        this.user = user;
        this.contactId = contactId;
    }

    private AddUserContactCommand(Parcel in) {
        this((RxBusDriver) in.readValue(CL), (User) in.readValue(CL), (String) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return addUserContact(service, rxBus, user, contactId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeValue(user);
        dest.writeValue(contactId);
    }
}
