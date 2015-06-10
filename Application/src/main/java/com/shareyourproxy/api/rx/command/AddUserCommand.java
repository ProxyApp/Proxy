package com.shareyourproxy.api.rx.command;

import android.app.IntentService;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxUserSync;
import com.shareyourproxy.api.rx.command.event.CommandEvent;

import java.util.List;

/**
 * Created by Evan on 6/9/15.
 */
public class AddUserCommand extends BaseCommand {
    public static final Parcelable.Creator<AddUserCommand> CREATOR =
        new Parcelable.Creator<AddUserCommand>() {
            @Override
            public AddUserCommand createFromParcel(Parcel in) {
                return new AddUserCommand(in);
            }

            @Override
            public AddUserCommand[] newArray(int size) {
                return new AddUserCommand[size];
            }
        };

    private final static java.lang.ClassLoader CL = AddUserCommand.class.getClassLoader();
    public final User user;

    public AddUserCommand(@NonNull User user) {
        super(AddUserCommand.class.getPackage().getName(),
            AddUserCommand.class.getName());
        this.user = user;
    }

    public AddUserCommand(BaseCommand command) {
        super(AddUserCommand.class.getPackage().getName(),
            AddUserCommand.class.getName());
        this.user = ((AddUserCommand) command).user;
    }


    private AddUserCommand(Parcel in) {
        this((User) in.readValue(CL));
    }

    @Override
    public List<CommandEvent> execute(IntentService service) {
        return RxUserSync.saveUser(service, user);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
    }

}
