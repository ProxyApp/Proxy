package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

import static com.shareyourproxy.api.rx.RxMessageSync.getFirebaseMessages;

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
    private final String userId;

    public GetUserMessagesCommand(
        @NonNull RxBusDriver rxBus,String userId) {
        super(GetUserMessagesCommand.class.getPackage().getName(),
            GetUserMessagesCommand.class.getName(),rxBus);
        this.userId = userId;
    }

    private GetUserMessagesCommand(Parcel in) {
        this((RxBusDriver) in.readValue(CL), (String) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return getFirebaseMessages(service, rxBus, userId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeValue(userId);
    }
}
