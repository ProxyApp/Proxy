package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

import static com.shareyourproxy.api.rx.RxUserSync.saveUser;

/**
 * Created by Evan on 6/9/15.
 */
public class AddUserCommand extends BaseCommand {
    public static final Parcelable.Creator<AddUserCommand> CREATOR =
        new Parcelable.Creator<AddUserCommand>() {
            @Override
            public AddUserCommand createFromParcel(Parcel in) {
                return new AddUserCommand(in);
            }

            @Override
            public AddUserCommand[] newArray(int size) {
                return new AddUserCommand[size];
            }
        };

    private final static java.lang.ClassLoader CL = AddUserCommand.class.getClassLoader();
    public final User user;

    public AddUserCommand(
        @NonNull RxBusDriver rxBus, @NonNull User user) {
        super(AddUserCommand.class.getPackage().getName(),
            AddUserCommand.class.getName(), rxBus);
        this.user = user;
    }

    private AddUserCommand(Parcel in) {
        this((RxBusDriver) in.readValue(CL), (User) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return saveUser(service, rxBus, user);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeValue(user);
    }

}
