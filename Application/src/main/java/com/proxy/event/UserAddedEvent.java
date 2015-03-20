package com.proxy.event;

import android.support.annotation.NonNull;

import com.proxy.model.User;

import timber.log.Timber;

import static com.proxy.util.DebugUtils.getDebugTAG;


/**
 * Event to pass around a user.
 */
public class UserAddedEvent {
    private static final String TAG = getDebugTAG(UserAddedEvent.class);
    public final User user;

    /**
     * Public constructor.
     *
     * @param user this events user
     */
    public UserAddedEvent(@NonNull User user) {
        Timber.v(TAG + user);
        this.user = user;
    }
}
