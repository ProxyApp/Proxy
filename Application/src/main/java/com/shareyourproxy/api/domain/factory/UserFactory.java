package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.domain.realm.RealmUser;

import java.util.ArrayList;

import io.realm.RealmResults;

import static com.shareyourproxy.api.domain.factory.ChannelFactory.getModelChannels;
import static com.shareyourproxy.api.domain.factory.ContactFactory.getModelContacts;
import static com.shareyourproxy.api.domain.factory.GroupFactory.getModelGroups;


/**
 * Factory for creating domain model {@link User}s.
 */
public class UserFactory {

    /**
     * Stupid Constructor.
     */
    private UserFactory() {
    }

    private static Id getUserId(String userId) {
        return Id.builder().value(userId).build();
    }

    public static User createModelUser(RealmUser realmUser) {
        if (realmUser != null) {
            return User.create(getUserId(realmUser.getId()), realmUser.getFirst(),
                realmUser.getLast(), realmUser.getEmail(), realmUser.getImageURL(),
                getModelChannels(realmUser.getChannels()), getModelGroups(realmUser
                    .getGroups()), getModelContacts(realmUser.getContacts()));
        }
        return null;
    }

    public static User createModelUser(Contact contact) {
        return User.create(contact.id(), contact.first(), contact.last(), null,
            contact.imageURL(), contact.channels(), null, null);
    }

    public static ArrayList<User> createModelUsers(RealmResults<RealmUser> realmUsers) {
        ArrayList<User> users = new ArrayList<>();
        for (RealmUser realmUser : realmUsers) {
            users.add(createModelUser(realmUser));
        }
        return users;
    }

    public static User addUserContact(User user, Contact contact) {
        if (user.contacts() != null) {
            user.contacts().add(contact);
        }
        return User.create(user.id(), user.first(), user.last(), user.email(),
            user.imageURL(), user.channels(), user.groups(), user.contacts());
    }

    /**
     * Create the same {@link User} with the updated email value.
     *
     * @param user to copy
     * @return updated user
     */
    public static User addUserChannel(User user, Channel channel) {
        ArrayList<Channel> channelArrayList;
        if (user.channels() != null) {
            channelArrayList = user.channels();
        } else {
            channelArrayList = new ArrayList<>(1);
        }
        channelArrayList.add(channel);
        return User.create(user.id(), user.first(), user.last(), user.email(),
            user.imageURL(), channelArrayList, user.groups(), user.contacts());
    }

    /**
     * Create the same {@link User} with the updated List<{@link Group}> values.
     *
     * @param user to copy
     * @return updated user
     */
    public static User addUserGroup(User user, Group newGroup) {
        //TODO: MAYBE THIS SHOULD BE A HASHSET SOMEHOW
        for(Group group : user.groups()){
            if(group.id().value().equals(newGroup.id().value())){
                user.groups().remove(group);
            }
        }
        user.groups().add(newGroup);
        return addUserGroups(user, user.groups());
    }

    public static User deleteUserGroup(User user, Group group) {
        user.groups().remove(group);
        return addUserGroups(user, user.groups());
    }

    /**
     * Create the same {@link User} with the updated List<{@link Group}> values.
     *
     * @param user   to copy
     * @param groups to update
     * @return updated user
     */
    public static User addUserGroups(User user, ArrayList<Group> groups) {
        return User.create(user.id(), user.first(), user.last(), user.email(),
            user.imageURL(), user.channels(), groups, user.contacts());
    }

    public static User deleteUserContact(User user, Contact contact) {
        if (user.contacts() != null) {
            user.contacts().remove(contact);
        }
        return User.create(user.id(), user.first(), user.last(), user.email(),
            user.imageURL(), user.channels(), user.groups(), user.contacts());
    }

    public static User deleteUserChannel(User user, Channel channel) {
        if (user.channels() != null) {
            user.channels().remove(channel);
        }

        return User.create(user.id(), user.first(), user.last(), user.email(),
            user.imageURL(), user.channels(), user.groups(), user.contacts());
    }

}
