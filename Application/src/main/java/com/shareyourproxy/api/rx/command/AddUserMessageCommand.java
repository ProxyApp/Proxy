package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Messages;
import com.shareyourproxy.api.rx.RxMessageSync;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

/**
 * Created by Evan on 6/18/15.
 */
public class AddUserMessageCommand extends BaseCommand {
    public static final Creator<AddUserMessageCommand> CREATOR = new Creator<AddUserMessageCommand>() {
        @Override
        public AddUserMessageCommand createFromParcel(Parcel in) {
            return new AddUserMessageCommand(in);
        }

        @Override
        public AddUserMessageCommand[] newArray(int size) {
            return new AddUserMessageCommand[size];
        }
    };
    public final Messages message;
    private final static java.lang.ClassLoader CL = AddUserMessageCommand.class.getClassLoader();

    /**
     * Public constructor.
     *
     * @param message
     */
    public AddUserMessageCommand(@NonNull Messages message) {
        super(AddUserMessageCommand.class.getPackage().getName(),
            AddUserMessageCommand.class.getName());
        this.message = message;
    }

    private AddUserMessageCommand(Parcel in) {
        this((Messages) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return RxMessageSync.saveFirebaseMessage(service, message);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(message);
    }
}
