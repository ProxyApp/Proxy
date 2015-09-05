package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

import static com.shareyourproxy.api.rx.RxUserContactSync.deleteUserContact;

/**
 * Delete user contact.
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
    private final String contactId;

    public DeleteUserContactCommand(
        @NonNull RxBusDriver rxBus, User user, String contactId) {
        super(DeleteUserContactCommand.class.getPackage().getName(),
            DeleteUserContactCommand.class.getName(), rxBus);
        this.user = user;
        this.contactId = contactId;
    }

    private DeleteUserContactCommand(Parcel in) {
        this((RxBusDriver) in.readValue(CL), (User) in.readValue(CL), (String) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return deleteUserContact(service, rxBus, user, contactId);
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
