package com.shareyourproxy.api.rx.event;

import com.shareyourproxy.api.domain.model.User;

/**
 * Created by Evan on 4/26/15.
 */
public class UserSelectedEvent {

    public final User user;

    /**
     * Constructor.
     *
     * @param user that was selected
     */
    public UserSelectedEvent(User user) {
        this.user = user;
    }
}
