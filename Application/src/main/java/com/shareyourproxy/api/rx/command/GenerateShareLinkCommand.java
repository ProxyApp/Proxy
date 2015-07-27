package com.shareyourproxy.api.rx.command;

import android.app.Service;
import android.os.Parcel;

import com.shareyourproxy.api.domain.model.GroupEditContact;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxShareLink;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evan on 7/27/15.
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

    public final ArrayList<GroupEditContact> groups;
    public final User user;

    public GenerateShareLinkCommand(User user, ArrayList<GroupEditContact> groups) {
        super(GenerateShareLinkCommand.class.getPackage().getName(),
            GenerateShareLinkCommand.class.getName());
        this.user = user;
        this.groups = groups;
    }

    private GenerateShareLinkCommand(Parcel in) {
        this((User) in.readValue(CL), (ArrayList<GroupEditContact>) in.readValue(CL));
    }

    @Override
    public List<EventCallback> execute(Service service) {
        return RxShareLink.getShareLinkMessageObservable(service, user, groups);
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
