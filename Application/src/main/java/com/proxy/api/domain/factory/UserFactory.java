package com.proxy.api.domain.factory;

import com.proxy.api.domain.model.Channel;
import com.proxy.api.domain.model.Group;
import com.proxy.api.domain.model.User;
import com.proxy.api.domain.model.UserId;
import com.proxy.api.domain.realm.RealmUser;

import java.util.ArrayList;

import io.realm.RealmResults;
import timber.log.Timber;

import static com.proxy.api.domain.factory.ChannelFactory.getModelChannels;
import static com.proxy.api.domain.factory.ChannelFactory.getRealmChannels;
import static com.proxy.api.domain.factory.ContactFactory.getModelContacts;
import static com.proxy.api.domain.factory.ContactFactory.getRealmContacts;
import static com.proxy.api.domain.factory.GroupFactory.getModelGroups;
import static com.proxy.api.domain.factory.GroupFactory.getRealmGroups;
import static com.proxy.util.ObjectUtils.joinWithSpace;


/**
 * Factory for creating domain model beans.
 */
public class UserFactory {

    /**
     * Stupid Constructor.
     */
    private UserFactory() {
    }

    /**
     * Create the same {@link User} with the updated email value.
     *
     * @param user  to copy
     * @param email to update
     * @return updated user
     */
    public static User updateUserEmail(User user, String email) {
        return User.create(user.id(), user.first(), user.last(), email,
            user.imageURL(), user.channels(), user.groups(), user.contacts());
    }

    /**
     * Create the same {@link User} with the updated email value.
     *
     * @param user to copy
     * @return updated user
     */
    public static User updateUserChannel(User user, Channel channel) {
        ArrayList<Channel> channelArrayList;
        if (user.channels() != null) {
            channelArrayList = user.channels();
        } else {
            channelArrayList = new ArrayList<>();
        }

        channelArrayList.add(channel);

        return User.create(user.id(), user.first(), user.last(), user.email(),
            user.imageURL(), channelArrayList, user.groups(), user.contacts());
    }

    /**
     * Create the same {@link User} with the updated List<{@link Group}> values.
     *
     * @param user   to copy
     * @param groups to update
     * @return updated user
     */
    public static User updateUserGroups(User user, ArrayList<Group> groups) {
        return User.create(user.id(), user.first(), user.last(), user.email(),
            user.imageURL(), user.channels(), groups, user.contacts());
    }

    /**
     * Convert User to RealmUser
     *
     * @param user to convert
     * @return RealmUser
     */
    public static RealmUser createRealmUser(User user) {
        RealmUser realmUser = new RealmUser();
        if (user != null) {
            realmUser.setUserId(user.id().value());
            realmUser.setFirstName(user.first());
            realmUser.setLastName(user.last());
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

    private static UserId getUserId(String userId) {
        return UserId.builder().value(userId).build();
    }

    public static User createModelUser(RealmUser realmUser) {
        if (realmUser != null) {
            return User.create(getUserId(realmUser.getUserId()), realmUser.getFirstName(),
                realmUser.getLastName(), realmUser.getEmail(), realmUser.getImageURL(),
                getModelChannels(realmUser.getChannels()), getModelGroups(realmUser
                    .getGroups()), getModelContacts(realmUser.getContacts()));
        }
        return null;
    }

    public static ArrayList<User> createModelUsers(RealmResults<RealmUser> realmUsers) {
        ArrayList<User> users = new ArrayList<>();
        for (RealmUser realmUser : realmUsers) {
            users.add(createModelUser(realmUser));
        }
        return users;
    }
}
