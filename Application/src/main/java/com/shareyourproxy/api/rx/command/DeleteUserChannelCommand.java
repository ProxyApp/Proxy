package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

import static com.shareyourproxy.api.rx.RxUserChannelSync.deleteChannel;

/**
 * Delete a channel associated with a user.
 */
public class DeleteUserChannelCommand extends BaseCommand {

    public static final Creator<DeleteUserChannelCommand> CREATOR =
        new Creator<DeleteUserChannelCommand>() {
            @Override
            public DeleteUserChannelCommand createFromParcel(Parcel in) {
                return new DeleteUserChannelCommand(in);
            }

            @Override
            public DeleteUserChannelCommand[] newArray(int size) {
                return new DeleteUserChannelCommand[size];
            }
        };
    private final static java.lang.ClassLoader CL = DeleteUserChannelCommand.class.getClassLoader();
    public final Channel channel;
    public final User user;

    public DeleteUserChannelCommand(
        @NonNull RxBusDriver rxBus, @NonNull User user, @NonNull Channel channel) {
        super(DeleteUserChannelCommand.class.getPackage().getName(),
            DeleteUserChannelCommand.class.getName(), rxBus);
        this.user = user;
        this.channel = channel;
    }

    private DeleteUserChannelCommand(Parcel in) {
        this((RxBusDriver) in.readValue(CL),
            (User) in.readValue(CL), (Channel) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return deleteChannel(service, rxBus, user, channel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeValue(user);
        dest.writeValue(channel);
    }
}
