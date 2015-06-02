package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.domain.realm.RealmUser;

import java.util.List;

import io.realm.RealmList;
import timber.log.Timber;

import static com.shareyourproxy.api.domain.factory.RealmChannelFactory.getRealmChannels;
import static com.shareyourproxy.api.domain.factory.RealmContactFactory.getRealmContacts;
import static com.shareyourproxy.api.domain.factory.RealmGroupFactory.getRealmGroups;
import static com.shareyourproxy.util.ObjectUtils.joinWithSpace;

/**
 * Factory for creating {@link RealmUser}s.
 */
public class RealmUserFactory {
    /**
     * Convert User to RealmUser
     *
     * @param user to convert
     * @return RealmUser
     */
    public static RealmUser createRealmUser(User user) {
        RealmUser realmUser = new RealmUser();
        if (user != null) {
            realmUser.setId(user.id().value());
            realmUser.setFirst(user.first());
            realmUser.setLast(user.last());
            realmUser.setFullName(joinWithSpace(new String[]{ user.first(), user.last() }));
            realmUser.setEmail(user.email());
            realmUser.setImageURL(user.imageURL());
            realmUser.setChannels(getRealmChannels(user.channels()));
            realmUser.setContacts(getRealmContacts(user.contacts()));
            realmUser.setGroups(getRealmGroups(user.groups()));
        }
        Timber.i("User Conversion: " + user.toString());
        return realmUser;
    }

    public static List<RealmUser> createRealmUsers(List<User> users) {
        RealmList<RealmUser> realmUsers = new RealmList<>();
        for (User user : users) {
            realmUsers.add(createRealmUser(user));
        }
        return realmUsers;
    }
}
