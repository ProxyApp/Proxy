package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Group;
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
     * @param realmGroupArray to get groups from
     * @return RealmList of Contacts
     */
    public static ArrayList<Group> getModelGroups(RealmList<RealmGroup> realmGroupArray) {
        if (realmGroupArray != null) {
            ArrayList<Group> groups = new ArrayList<>();
            for (RealmGroup realmGroup : realmGroupArray) {
                groups.add(Group.create(realmGroup.getLabel(),
                    ChannelFactory.getModelChannels(realmGroup.getChannels()),
                    ContactFactory.getModelContacts(realmGroup.getContacts())));
            }
            return groups;
        }
        return null;
    }
}
