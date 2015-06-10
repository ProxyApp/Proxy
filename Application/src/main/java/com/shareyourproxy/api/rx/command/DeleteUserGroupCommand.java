package com.shareyourproxy.api.rx.command;

import android.app.IntentService;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxUserGroupSync;
import com.shareyourproxy.api.rx.command.event.CommandEvent;

import java.util.List;

/**
 * Created by Evan on 6/8/15.
 */
public class DeleteUserGroupCommand extends BaseCommand {
    public static final Creator<DeleteUserGroupCommand> CREATOR =
        new Creator<DeleteUserGroupCommand>() {
            @Override
            public DeleteUserGroupCommand createFromParcel(Parcel in) {
                return new DeleteUserGroupCommand(in);
            }

            @Override
            public DeleteUserGroupCommand[] newArray(int size) {
                return new DeleteUserGroupCommand[size];
            }
        };
    private final static java.lang.ClassLoader CL = DeleteUserGroupCommand.class.getClassLoader();
    public final Group group;
    public final User user;

    /**
     * Public constructor.
     *
     * @param user  logged in user
     * @param group this events group
     */
    public DeleteUserGroupCommand(@NonNull User user, @NonNull Group group) {
        super(DeleteUserGroupCommand.class.getPackage().getName(),
            DeleteUserGroupCommand.class.getName());
        this.user = user;
        this.group = group;
    }

    public DeleteUserGroupCommand(BaseCommand command) {
        super(DeleteUserGroupCommand.class.getPackage().getName(),
            DeleteUserGroupCommand.class.getName());
        this.user = ((DeleteUserGroupCommand) command).user;
        this.group = ((DeleteUserGroupCommand) command).group;
    }

    private DeleteUserGroupCommand(Parcel in) {
        this((User) in.readValue(CL), (Group) in.readValue(CL));
    }

    @Override
    public List<CommandEvent> execute(IntentService service) {
        return RxUserGroupSync.deleteUserGroup(service, user, group);
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
