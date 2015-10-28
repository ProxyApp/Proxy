package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;

import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxUserSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

/**
 * Sync All Users data from firebase to Realm and return the logged in User.
 */
public class SyncContactsCommand extends BaseCommand {
    public static final Parcelable.Creator<SyncContactsCommand> CREATOR = new Parcelable
        .Creator<SyncContactsCommand>
        () {
        @Override
        public SyncContactsCommand createFromParcel(Parcel in) {
            return new SyncContactsCommand(in);
        }

        @Override
        public SyncContactsCommand[] newArray(int size) {
            return new SyncContactsCommand[size];
        }
    };

    private final static java.lang.ClassLoader CL =
        SyncContactsCommand.class.getClassLoader();
    public final User user;

    public SyncContactsCommand(User user) {
        this.user = user;
    }

    private SyncContactsCommand(Parcel in) {
        this((User) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return RxUserSync.syncAllContacts(service, user);
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
