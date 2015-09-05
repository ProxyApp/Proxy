package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.RxUserSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

/**
 * Sync All Users data from firebase to Realm and return the logged in User.
 */
public class SyncAllUsersCommand extends BaseCommand {
    public static final Parcelable.Creator<SyncAllUsersCommand> CREATOR = new Parcelable
        .Creator<SyncAllUsersCommand>
        () {
        @Override
        public SyncAllUsersCommand createFromParcel(Parcel in) {
            return new SyncAllUsersCommand(in);
        }

        @Override
        public SyncAllUsersCommand[] newArray(int size) {
            return new SyncAllUsersCommand[size];
        }
    };

    private final static java.lang.ClassLoader CL =
        SyncAllUsersCommand.class.getClassLoader();
    public final String userId;

    public SyncAllUsersCommand(
        @NonNull RxBusDriver rxBus, String userId) {
        super(SyncAllUsersCommand.class.getPackage().getName(),
            SyncAllUsersCommand.class.getName(), rxBus);
        this.userId = userId;
    }

    private SyncAllUsersCommand(Parcel in) {
        this((RxBusDriver) in.readValue(CL), (String) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return RxUserSync.syncAllUsers(service, rxBus, userId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeValue(userId);
    }
}
