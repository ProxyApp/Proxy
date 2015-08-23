package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxGroupChannelSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

/**
 * Created by Evan on 7/8/15.
 */
public class AddUserGroupsChannelCommand extends BaseCommand {
    public static final Parcelable.Creator<AddUserGroupsChannelCommand> CREATOR =
        new Parcelable.Creator<AddUserGroupsChannelCommand>() {
            @Override
            public AddUserGroupsChannelCommand createFromParcel(Parcel in) {
                return new AddUserGroupsChannelCommand(in);
            }

            @Override
            public AddUserGroupsChannelCommand[] newArray(int size) {
                return new AddUserGroupsChannelCommand[size];
            }
        };
    private final static java.lang.ClassLoader CL =
        AddUserGroupsChannelCommand.class.getClassLoader();
    public final Channel channel;
    public final User user;

    /**
     * Public constructor.
     *
     * @param user    logged in user
     * @param channel this events group
     */
    public AddUserGroupsChannelCommand(@NonNull User user, @NonNull Channel channel) {
        super(AddUserGroupsChannelCommand.class.getPackage().getName(),
            AddUserGroupsChannelCommand.class.getName());
        this.user = user;
        this.channel = channel;
    }

    private AddUserGroupsChannelCommand(Parcel in) {
        this((User) in.readValue(CL), (Channel) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return RxGroupChannelSync.addUserGroupsChannel(service, user, channel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(channel);
    }
}
