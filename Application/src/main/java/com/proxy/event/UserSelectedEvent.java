package com.proxy.event;

import com.proxy.api.domain.factory.UserFactory;
import com.proxy.api.domain.model.User;
import com.proxy.api.domain.realm.RealmUser;

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
