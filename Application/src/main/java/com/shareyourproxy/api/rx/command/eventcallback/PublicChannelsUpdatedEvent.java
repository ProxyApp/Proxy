package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;

import java.util.HashMap;

/**
 * Created by Evan on 10/1/15.
 */
public class PublicChannelsUpdatedEvent extends UserEventCallback {
    private final static java.lang.ClassLoader CL =
        PublicChannelsUpdatedEvent.class.getClassLoader();
    public static final Creator<PublicChannelsUpdatedEvent> CREATOR =
        new Creator<PublicChannelsUpdatedEvent>() {
            @Override
            public PublicChannelsUpdatedEvent createFromParcel(Parcel in) {
                return new PublicChannelsUpdatedEvent(
                    (User) in.readValue(CL), (HashMap<String, Channel>) in.readValue(CL));
            }

            @Override
            public PublicChannelsUpdatedEvent[] newArray(int size) {
                return new PublicChannelsUpdatedEvent[size];
            }
        };
    public final HashMap<String, Channel> newChannels;

    public PublicChannelsUpdatedEvent(User user, HashMap<String, Channel> newChannels) {
        super(user);
        this.newChannels = newChannels;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
