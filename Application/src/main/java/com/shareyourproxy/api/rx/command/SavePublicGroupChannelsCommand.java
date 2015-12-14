package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;

import com.shareyourproxy.api.domain.model.ChannelToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxGroupChannelSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.ArrayList;


/**
 * Created by Evan on 10/1/15.
 */
public class SavePublicGroupChannelsCommand extends BaseCommand {
    public static final Parcelable.Creator<SavePublicGroupChannelsCommand> CREATOR =
        new Parcelable.Creator<SavePublicGroupChannelsCommand>() {
            @Override
            public SavePublicGroupChannelsCommand createFromParcel(Parcel in) {
                return new SavePublicGroupChannelsCommand(in);
            }

            @Override
            public SavePublicGroupChannelsCommand[] newArray(int size) {
                return new SavePublicGroupChannelsCommand[size];
            }
        };
    private final static java.lang.ClassLoader CL =
        SavePublicGroupChannelsCommand.class.getClassLoader();

    private final User user;
    private final ArrayList<ChannelToggle> channels;


    public SavePublicGroupChannelsCommand(User user, ArrayList<ChannelToggle> channels) {
        this.user = user;
        this.channels = channels;
    }

    private SavePublicGroupChannelsCommand(Parcel in) {
        this((User) in.readValue(CL), (ArrayList<ChannelToggle>) in.readValue(CL));
    }

    @Override
    public EventCallback execute(Service service) {
        return RxGroupChannelSync.INSTANCE.updatePublicGroupChannels(service, user, channels);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(channels);
    }
}
