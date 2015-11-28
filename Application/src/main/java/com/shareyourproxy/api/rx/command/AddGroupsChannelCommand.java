package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.ArrayList;
import java.util.List;

import static com.shareyourproxy.api.rx.RxGroupChannelSync.addUserGroupsChannel;

/**
 * Created by Evan on 7/8/15.
 */
public class AddGroupsChannelCommand extends BaseCommand {
    public static final Parcelable.Creator<AddGroupsChannelCommand> CREATOR =
        new Parcelable.Creator<AddGroupsChannelCommand>() {
            @Override
            public AddGroupsChannelCommand createFromParcel(Parcel in) {
                return new AddGroupsChannelCommand(in);
            }

            @Override
            public AddGroupsChannelCommand[] newArray(int size) {
                return new AddGroupsChannelCommand[size];
            }
        };
    private final static java.lang.ClassLoader CL =
        AddGroupsChannelCommand.class.getClassLoader();
    public final User user;
    public final Channel channel;
    public final ArrayList<GroupToggle> groups;

    /**
     * Public constructor.
     *
     * @param user    Logged in user
     * @param groups  selected groups to add channel to
     * @param channel this events group
     */
    public AddGroupsChannelCommand(
        @NonNull User user, @NonNull ArrayList<GroupToggle> groups,
        @NonNull Channel channel) {
        this.user = user;
        this.groups = groups;
        this.channel = channel;
    }

    private AddGroupsChannelCommand(Parcel in) {
        this((User) in.readValue(CL), (ArrayList<GroupToggle>) in.readValue(CL),
            (Channel) in.readValue(CL));
    }

    @Override
    public EventCallback execute(Service service) {
        return addUserGroupsChannel(service, user, groups, channel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(groups);
        dest.writeValue(channel);
    }
}
