package com.shareyourproxy.api.rx.command;

import android.app.IntentService;
import android.os.Parcel;
import android.os.Parcelable;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxGroupChannelSync;
import com.shareyourproxy.api.rx.command.callback.CommandEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evan on 6/11/15.
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

    public final User user;
    public final String newTitle;
    public final Group group;
    public final ArrayList<Channel> channels;


    public SaveGroupChannelsCommand(
        User user, String newTitle, Group group, ArrayList<Channel> channels) {
        super(SaveGroupChannelsCommand.class.getPackage().getName(),
            SaveGroupChannelsCommand.class.getName());
        this.user = user;
        this.newTitle = newTitle;
        this.group = group;
        this.channels = channels;
    }

    public SaveGroupChannelsCommand(BaseCommand command) {
        super(SaveGroupChannelsCommand.class.getPackage().getName(),
            SaveGroupChannelsCommand.class.getName());
        this.user = ((SaveGroupChannelsCommand) command).user;
        this.newTitle = ((SaveGroupChannelsCommand) command).newTitle;
        this.group = ((SaveGroupChannelsCommand) command).group;
        this.channels = ((SaveGroupChannelsCommand) command).channels;
    }

    private SaveGroupChannelsCommand(Parcel in) {
        this((User) in.readValue(CL), (String) in.readValue(CL),
            (Group) in.readValue(CL), (ArrayList<Channel>) in.readValue(CL));
    }

    @Override
    public List<CommandEvent> execute(IntentService service) {
        return RxGroupChannelSync
            .updateGroupChannels(service, user, newTitle, group, channels);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(newTitle);
        dest.writeValue(group);
        dest.writeValue(channels);
    }

}
