package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserGroupAddedEventCallback;

import java.util.ArrayList;
import java.util.List;

import static com.shareyourproxy.api.domain.factory.ChannelFactory.createPublicChannel;
import static com.shareyourproxy.api.rx.RxGroupChannelSync.addUserGroupsChannel;
import static com.shareyourproxy.api.rx.RxUserChannelSync.saveUserChannel;

/**
 * Created by Evan on 7/8/15.
 */
public class AddGroupChannelAndPublicCommand extends BaseCommand {
    public static final Parcelable.Creator<AddGroupChannelAndPublicCommand> CREATOR =
        new Parcelable.Creator<AddGroupChannelAndPublicCommand>() {
            @Override
            public AddGroupChannelAndPublicCommand createFromParcel(Parcel in) {
                return new AddGroupChannelAndPublicCommand(in);
            }

            @Override
            public AddGroupChannelAndPublicCommand[] newArray(int size) {
                return new AddGroupChannelAndPublicCommand[size];
            }
        };
    private final static java.lang.ClassLoader CL =
        AddGroupChannelAndPublicCommand.class.getClassLoader();
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
    public AddGroupChannelAndPublicCommand(
        @NonNull User user, @NonNull ArrayList<GroupToggle> groups, @NonNull Channel channel) {
        this.user = user;
        this.groups = groups;
        this.channel = channel;
    }

    private AddGroupChannelAndPublicCommand(Parcel in) {
        this((User) in.readValue(CL), (ArrayList<GroupToggle>) in.readValue(CL),
            (Channel) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        List<EventCallback> results = new ArrayList<>();
        Channel publicChannel = createPublicChannel(channel, true);
        //TODO clean this mess up
        results.addAll(addUserGroupsChannel(service, user, groups, publicChannel));
        UserGroupAddedEventCallback event = (UserGroupAddedEventCallback) results.get(0);
        results.addAll(saveUserChannel(service, event.user, channel, publicChannel));
        return results;
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
