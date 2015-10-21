package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.User;

import java.util.HashMap;

/**
 * Created by Evan on 10/1/15.
 */
public class PublicChannelsUpdatedEvent extends UserEventCallback{
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
