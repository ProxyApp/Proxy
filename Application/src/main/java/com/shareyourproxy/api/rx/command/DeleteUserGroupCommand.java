package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxUserGroupSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

/**
 * Delete a group associated with a User.
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
        this.user = user;
        this.group = group;
    }

    private DeleteUserGroupCommand(Parcel in) {
        this((User) in.readValue(CL), (Group) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
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
