package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.realm.RealmGroup;

import java.util.ArrayList;

import io.realm.RealmList;

/**
 * Factory for creating domain model {@link Group}s.
 */
public class GroupFactory {

    /**
     * Return a RealmList of Contacts from a user
     *
     * @param realmGroupArray to get contactGroups from
     * @return RealmList of Contacts
     */
    public static ArrayList<Group> getModelGroups(RealmList<RealmGroup> realmGroupArray) {
        ArrayList<Group> groups = new ArrayList<>(realmGroupArray.size());
        for (RealmGroup realmGroup : realmGroupArray) {
            groups.add(Group.copy(Id.create(realmGroup.getId()), realmGroup.getLabel(),
                ChannelFactory.getModelChannels(realmGroup.getChannels()),
                ContactFactory.getModelContacts(realmGroup.getContacts())));
        }
        return groups;
    }

    public static Group addGroupContact(Group group, Contact contact) {
        ArrayList<Contact> contacts = group.contacts();
        if (contacts == null) {
            contacts = new ArrayList<>();
        }
        contacts.add(contact);
        return Group.copy(group.id(), group.label(), group.channels(), contacts);
    }

    public static Group deleteGroupContact(Group group, Contact contact) {
        ArrayList<Contact> contacts = group.contacts();
        if (contacts != null) {
            contacts.remove(contact);
        }
        return Group.copy(group.id(), group.label(), group.channels(), contacts);
    }

    public static Group addGroupChannels(
        String newTitle, Group oldGroup, ArrayList<Channel> channels) {
        return Group.copy(oldGroup.id(), newTitle, channels, oldGroup.contacts());
    }
}
