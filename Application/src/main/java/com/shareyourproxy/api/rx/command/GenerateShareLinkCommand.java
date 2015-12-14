package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;

import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxShareLink;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.ArrayList;


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
    public final User user;

    public GenerateShareLinkCommand(User loggedInUser, ArrayList<GroupToggle> groups) {
        this.groups = groups;
        this.user = loggedInUser;
    }

    private GenerateShareLinkCommand(Parcel in) {
        this((User) in.readValue(CL), (ArrayList<GroupToggle>) in.readValue(CL));
    }

    @Override
    public EventCallback execute(Service service) {
        return RxShareLink.INSTANCE.getShareLinkMessageObservable(service, user, groups);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(groups);
    }


}
