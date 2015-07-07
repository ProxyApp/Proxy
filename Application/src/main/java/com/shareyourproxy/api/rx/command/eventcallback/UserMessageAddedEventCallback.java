package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.shareyourproxy.api.domain.model.Message;

/**
 * Created by Evan on 6/18/15.
 */
public class UserMessageAddedEventCallback extends EventCallback {
    public final Message message;

    /**
     * Public constructor.
     *
     * @param message notification content
     */
    public UserMessageAddedEventCallback(@NonNull Message message) {
        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(message, flags);
    }
}
