package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.domain.realm.RealmUser;

import java.util.ArrayList;
import java.util.Iterator;

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
        ArrayList<Contact> contactArrayList = user.contacts();
        if (contactArrayList == null) {
            contactArrayList = new ArrayList<>(1);
        }
        contactArrayList.add(contact);
        return User.create(user.id(), user.first(), user.last(), user.email(),
            user.imageURL(), user.channels(), user.groups(), contactArrayList);
    }

    /**
     * Create the same {@link User} with the updated email value.
     *
     * @param user to copy
     * @return updated user
     */
    public static User addUserChannel(User user, Channel channel) {
        ArrayList<Channel> channelArrayList = user.channels();
        if (channelArrayList == null) {
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
        ArrayList<Group> groups = user.groups();
        for (Iterator<Group> iterator = groups.iterator(); iterator.hasNext(); ) {
            Group group = iterator.next();
            if (group.id().value().equals(newGroup.id().value())) {
                iterator.remove();
            }
        }
        groups.add(newGroup);
        return addUserGroups(user, groups);
    }

    public static User deleteUserGroup(User user, Group group) {
        ArrayList<Group> groups = user.groups();
        groups.remove(group);
        return addUserGroups(user, groups);
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
        ArrayList<Contact> contacts = user.contacts();
        if (contacts != null) {
            contacts.remove(contact);
        }
        return User.create(user.id(), user.first(), user.last(), user.email(),
            user.imageURL(), user.channels(), user.groups(), contacts);
    }

    public static User deleteUserChannel(User user, Channel channel) {
        ArrayList<Channel> channels = user.channels();
        if (channels != null) {
            channels.remove(channel);
        }
        return User.create(user.id(), user.first(), user.last(), user.email(),
            user.imageURL(), channels, user.groups(), user.contacts());
    }
}
