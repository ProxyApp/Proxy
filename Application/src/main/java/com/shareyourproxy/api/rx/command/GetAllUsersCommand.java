package com.shareyourproxy.api.rx.command;

import android.app.IntentService;
import android.os.Parcel;

import com.shareyourproxy.api.rx.RxUserSync;
import com.shareyourproxy.api.rx.command.event.CommandEvent;

import java.util.List;

/**
 * Created by Evan on 6/9/15.
 */
public class GetAllUsersCommand extends BaseCommand {

    public static final Creator<GetAllUsersCommand> CREATOR = new Creator<GetAllUsersCommand>
        () {
        @Override
        public GetAllUsersCommand createFromParcel(Parcel in) {
            return new GetAllUsersCommand(in);
        }

        @Override
        public GetAllUsersCommand[] newArray(int size) {
            return new GetAllUsersCommand[size];
        }
    };
    private final static java.lang.ClassLoader CL = GetAllUsersCommand.class.getClassLoader();

    public GetAllUsersCommand() {
        super(GetAllUsersCommand.class.getPackage().getName(),
            GetAllUsersCommand.class.getName());
    }

    public GetAllUsersCommand(BaseCommand command) {
        super(GetAllUsersCommand.class.getPackage().getName(),
            GetAllUsersCommand.class.getName());
    }

    private GetAllUsersCommand(Parcel in) {
        this();
    }

    @Override
    public List<CommandEvent> execute(IntentService service) {
        return RxUserSync.getAllUsers(service);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
