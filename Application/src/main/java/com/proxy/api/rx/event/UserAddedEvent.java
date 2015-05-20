package com.proxy.api.rx.event;

import android.support.annotation.NonNull;

import com.proxy.api.domain.model.User;

import timber.log.Timber;



/**
 * Event to pass around a user.
 */
public class UserAddedEvent {
    public final User user;

    /**
     * Public constructor.
     *
     * @param user this events user
     */
    public UserAddedEvent(@NonNull User user) {
        Timber.v(user.toString());
        this.user = user;
    }
}
