package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.app.EditGroupChannelsActivity.GroupEditType;

import java.util.HashSet;
import java.util.List;

import static com.shareyourproxy.api.rx.RxGroupChannelSync.updateGroupChannels;

/**
 * Save channels associated with a group.
 */
public class SaveGroupChannelsCommand extends BaseCommand {
    public static final Parcelable.Creator<SaveGroupChannelsCommand> CREATOR =
        new Parcelable.Creator<SaveGroupChannelsCommand>() {
            @Override
            public SaveGroupChannelsCommand createFromParcel(Parcel in) {
                return new SaveGroupChannelsCommand(in);
            }

            @Override
            public SaveGroupChannelsCommand[] newArray(int size) {
                return new SaveGroupChannelsCommand[size];
            }
        };
    private final static java.lang.ClassLoader CL =
        SaveGroupChannelsCommand.class.getClassLoader();

    private final User user;
    private final String newTitle;
    private final Group group;
    private final HashSet<String> channels;
    private final GroupEditType groupEditType;


    public SaveGroupChannelsCommand(
        @NonNull RxBusDriver rxBus, User user, String newTitle, Group group,
        HashSet<String> channels, GroupEditType groupEditType) {
        super(SaveGroupChannelsCommand.class.getPackage().getName(),
            SaveGroupChannelsCommand.class.getName(), rxBus);
        this.user = user;
        this.newTitle = newTitle;
        this.group = group;
        this.channels = channels;
        this.groupEditType = groupEditType;
    }

    private SaveGroupChannelsCommand(Parcel in) {
        this((RxBusDriver) in.readValue(CL),
            (User) in.readValue(CL), (String) in.readValue(CL),
            (Group) in.readValue(CL), (HashSet<String>) in.readValue(CL),
            (GroupEditType) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return updateGroupChannels(service, rxBus, user, newTitle,
            group, channels, groupEditType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeValue(user);
        dest.writeValue(newTitle);
        dest.writeValue(group);
        dest.writeValue(channels);
        dest.writeValue(groupEditType);
    }

}
