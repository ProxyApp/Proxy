package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxGroupChannelSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.HashMap;
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
    public final HashMap<String, Id> channels;


    public SaveGroupChannelsCommand(
        User user, String newTitle, Group group, HashMap<String, Id> channels) {
        super(SaveGroupChannelsCommand.class.getPackage().getName(),
            SaveGroupChannelsCommand.class.getName());
        this.user = user;
        this.newTitle = newTitle;
        this.group = group;
        this.channels = channels;
    }

    private SaveGroupChannelsCommand(Parcel in) {
        this((User) in.readValue(CL), (String) in.readValue(CL),
            (Group) in.readValue(CL), (HashMap<String, Id>) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
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
