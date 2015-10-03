package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.ChannelToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.ArrayList;
import java.util.List;

import static com.shareyourproxy.api.rx.RxGroupChannelSync.updatePublicGroupChannels;

/**
 * Created by Evan on 10/1/15.
 */
public class SavePublicGroupChannelsCommand extends BaseCommand{
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


    public SavePublicGroupChannelsCommand(
        @NonNull RxBusDriver rxBus, User user, ArrayList<ChannelToggle> channels) {
        super(SavePublicGroupChannelsCommand.class.getPackage().getName(),
            SavePublicGroupChannelsCommand.class.getName(), rxBus);
        this.user = user;
        this.channels = channels;
    }

    private SavePublicGroupChannelsCommand(Parcel in) {
        this((RxBusDriver) in.readValue(CL), (User) in.readValue(CL),
            (ArrayList<ChannelToggle>) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return updatePublicGroupChannels(service, rxBus, user, channels);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeValue(user);
        dest.writeValue(channels);
    }
}
