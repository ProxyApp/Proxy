package com.shareyourproxy.api.rx.command;

import android.app.IntentService;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxUserChannelSync;
import com.shareyourproxy.api.rx.command.callback.CommandEvent;

import java.util.List;

/**
 * Created by Evan on 6/8/15.
 */
public class AddUserChannelCommand extends BaseCommand {
    public static final Creator<AddUserChannelCommand> CREATOR = new Creator<AddUserChannelCommand>
        () {
        @Override
        public AddUserChannelCommand createFromParcel(Parcel in) {
            return new AddUserChannelCommand(in);
        }

        @Override
        public AddUserChannelCommand[] newArray(int size) {
            return new AddUserChannelCommand[size];
        }
    };
    private final static java.lang.ClassLoader CL = AddUserChannelCommand.class.getClassLoader();
    public final Channel channel;
    public final User user;

    public AddUserChannelCommand(@NonNull User user, @NonNull Channel channel) {
        super(AddUserChannelCommand.class.getPackage().getName(),
            AddUserChannelCommand.class.getName());
        this.user = user;
        this.channel = channel;
    }

    public AddUserChannelCommand(BaseCommand command) {
        super(AddUserChannelCommand.class.getPackage().getName(),
            AddUserChannelCommand.class.getName());
        this.user = ((AddUserChannelCommand) command).user;
        this.channel = ((AddUserChannelCommand) command).channel;
    }


    private AddUserChannelCommand(Parcel in) {
        this((User) in.readValue(CL), (Channel) in.readValue(CL));
    }

    @Override
    public List<CommandEvent> execute(IntentService service) {
        return RxUserChannelSync.addChannel(service, user, channel);
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
