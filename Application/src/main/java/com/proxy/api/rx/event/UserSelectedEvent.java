package com.proxy.api.rx.event;

import com.proxy.api.domain.factory.UserFactory;
import com.proxy.api.domain.model.User;
import com.proxy.api.domain.realm.RealmUser;

import timber.log.Timber;

import static com.proxy.api.domain.factory.UserFactory.printRealmUser;

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
        Timber.i("Realm User Selected: " + printRealmUser(user));
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
