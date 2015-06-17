package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;

import com.shareyourproxy.api.rx.RxUserSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

/**
 * Get all users from Firebase.
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

    public GetAllUsersCommand() {
        super(GetAllUsersCommand.class.getPackage().getName(),
            GetAllUsersCommand.class.getName());
    }

    private GetAllUsersCommand(Parcel in) {
        this();
    }

    @Override
    public List<EventCallback> execute(Service service) {
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
