package com.shareyourproxy.api.rx.command;

import android.app.IntentService;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxUserGroupSync;
import com.shareyourproxy.api.rx.command.callback.CommandEvent;

import java.util.List;

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
    public final Group group;
    public final User user;
    private final static java.lang.ClassLoader CL = AddUserGroupCommand.class.getClassLoader();

    /**
     * Public constructor.
     *
     * @param user  logged in user
     * @param group this events group
     */
    public AddUserGroupCommand(@NonNull User user, @NonNull Group group) {
        super(AddUserGroupCommand.class.getPackage().getName(),
            AddUserGroupCommand.class.getName());
        this.user = user;
        this.group = group;
    }

    public AddUserGroupCommand(BaseCommand command) {
        super(AddUserGroupCommand.class.getPackage().getName(),
            AddUserGroupCommand.class.getName());
        this.user = ((AddUserGroupCommand) command).user;
        this.group = ((AddUserGroupCommand) command).group;
    }

    private AddUserGroupCommand(Parcel in) {
        this((User) in.readValue(CL), (Group) in.readValue(CL));
    }

    @Override
    public List<CommandEvent> execute(IntentService service) {
        return RxUserGroupSync.addUserGroup(service, user, group);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(group);
    }
}
