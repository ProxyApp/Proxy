package com.shareyourproxy.api.rx.event;

import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.domain.realm.RealmUser;

import timber.log.Timber;

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
    public UserSelectedEvent(RealmUser user) {
        this.user = UserFactory.createModelUser(user);
        Timber.i("User Selected: " + user.toString());
    }

    /**
     * Constructor.
     *
     * @param user that was selected
     */
    public UserSelectedEvent(User user) {
        this.user = user;
    }


}
