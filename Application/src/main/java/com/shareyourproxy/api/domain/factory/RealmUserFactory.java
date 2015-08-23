package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.domain.realm.RealmUser;

import java.util.Map;

import io.realm.RealmList;

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
            realmUser.setProfileURL(user.profileURL());
            realmUser.setCoverURL(user.coverURL());
            realmUser.setChannels(getRealmChannels(user.channels()));
            realmUser.setContacts(getRealmContacts(user.contacts()));
            realmUser.setGroups(getRealmGroups(user.groups()));
            if (user.version() == null) {
                realmUser.setVersion(0);
            } else {
                realmUser.setVersion(user.version());
            }
        }
        return realmUser;
    }

    public static RealmList<RealmUser> createRealmUsers(Map<String, User> users) {
        RealmList<RealmUser> realmUsers = new RealmList<>();
        for (Map.Entry<String, User> user : users.entrySet()) {
            realmUsers.add(createRealmUser(user.getValue()));
        }
        return realmUsers;
    }
}
