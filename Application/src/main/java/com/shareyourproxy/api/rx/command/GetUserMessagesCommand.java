package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;

import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxMessageSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

/**
 * Created by Evan on 6/18/15.
 */
public class GetUserMessagesCommand extends BaseCommand {

    public static final Creator<GetUserMessagesCommand> CREATOR =
        new Creator<GetUserMessagesCommand>() {
        @Override
        public GetUserMessagesCommand createFromParcel(Parcel in) {
            return new GetUserMessagesCommand(in);
        }

        @Override
        public GetUserMessagesCommand[] newArray(int size) {
            return new GetUserMessagesCommand[size];
        }
    };
    private final static java.lang.ClassLoader CL = GetUserMessagesCommand.class.getClassLoader();
    private final User user;

    public GetUserMessagesCommand(User user) {
        super(GetUserMessagesCommand.class.getPackage().getName(),
            GetUserMessagesCommand.class.getName());
        this.user = user;
    }

    private GetUserMessagesCommand(Parcel in) {
        this((User) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return RxMessageSync.getFirebaseMessages(service, user);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
