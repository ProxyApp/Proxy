package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.ArrayList;
import java.util.List;

import static com.shareyourproxy.api.rx.RxShareLink.getShareLinkMessageObservable;

/**
 * Generate public link urls.
 */
public class GenerateShareLinkCommand extends BaseCommand {
    public static final Creator<GenerateShareLinkCommand> CREATOR =
        new Creator<GenerateShareLinkCommand>() {
            @Override
            public GenerateShareLinkCommand createFromParcel(Parcel in) {
                return new GenerateShareLinkCommand(in);
            }

            @Override
            public GenerateShareLinkCommand[] newArray(int size) {
                return new GenerateShareLinkCommand[size];
            }
        };
    private final static java.lang.ClassLoader CL =
        GenerateShareLinkCommand.class.getClassLoader();

    public final ArrayList<GroupToggle> groups;

    public GenerateShareLinkCommand(
        @NonNull RxBusDriver rxBus, ArrayList<GroupToggle> groups) {
        super(GenerateShareLinkCommand.class.getPackage().getName(),
            GenerateShareLinkCommand.class.getName(), rxBus);
        this.groups = groups;
    }

    private GenerateShareLinkCommand(Parcel in) {
        this((RxBusDriver) in.readValue(CL), (ArrayList<GroupToggle>) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return getShareLinkMessageObservable(service, rxBus, groups);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeValue(groups);
    }


}
