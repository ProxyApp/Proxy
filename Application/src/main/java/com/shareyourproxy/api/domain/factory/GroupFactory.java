package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.realm.RealmGroup;

import java.util.HashMap;
import java.util.HashSet;

import io.realm.RealmList;

import static com.shareyourproxy.api.domain.factory.ChannelFactory.getModelChannelList;
import static com.shareyourproxy.api.domain.factory.ContactFactory.getModelContactList;

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
    public static HashMap<String, Group> getModelGroups(RealmList<RealmGroup> realmGroupArray) {
        HashMap<String, Group> groups = new HashMap<>(realmGroupArray.size());
        for (RealmGroup realmGroup : realmGroupArray) {
            groups.put(realmGroup.getId(), getModelGroup(realmGroup));
        }
        return groups;
    }

    public static Group getModelGroup(RealmGroup realmGroup) {
        return Group.copy(realmGroup.getId(), realmGroup.getLabel(),
            getModelChannelList(realmGroup.getChannels()),
            getModelContactList(realmGroup.getContacts()));
    }

    public static Group addGroupChannels(
        String newTitle, Group oldGroup, HashSet<String> channels) {
        return Group.copy(oldGroup.id(), newTitle, channels, oldGroup.contacts());
    }

}
