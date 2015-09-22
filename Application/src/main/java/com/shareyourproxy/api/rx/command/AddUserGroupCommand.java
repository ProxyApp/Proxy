package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

import static com.shareyourproxy.api.rx.RxUserGroupSync.addUserGroup;

/**
 * Created by Evan on 6/8/15.
 */
public class AddUserGroupCommand extends BaseCommand {

    public static final Creator<AddUserGroupCommand> CREATOR = new Creator<AddUserGroupCommand>() {
        @Override
        public AddUserGroupCommand createFromParcel(Parcel in) {
            return new AddUserGroupCommand(in);
        }

        @Override
        public AddUserGroupCommand[] newArray(int size) {
            return new AddUserGroupCommand[size];
        }
    };
    private final static java.lang.ClassLoader CL = AddUserGroupCommand.class.getClassLoader();
    public final Group group;
    public final User user;

    /**
     * Public constructor.
     *
     * @param user  logged in user
     * @param group this events group
     */
    public AddUserGroupCommand(
        @NonNull RxBusDriver rxBus, @NonNull User user, @NonNull Group group) {
        super(AddUserGroupCommand.class.getPackage().getName(),
            AddUserGroupCommand.class.getName(), rxBus);
        this.user = user;
        this.group = group;
    }

    private AddUserGroupCommand(Parcel in) {
        this((RxBusDriver) in.readValue(CL), (User) in.readValue(CL), (Group) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return addUserGroup(service, rxBus, user, group);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeValue(user);
        dest.writeValue(group);
    }
}
