package com.proxy.api.domain.factory;

import com.proxy.api.domain.model.Group;
import com.proxy.api.domain.realm.RealmGroup;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

import static com.proxy.api.domain.factory.ChannelFactory.getRealmChannels;
import static com.proxy.api.domain.factory.ContactFactory.getRealmContacts;

/**
 * Factory for creating Groups.
 */
public class GroupFactory {

    /**
     * Return a RealmList of Contacts from a user
     *
     * @param groupArrayList to get contacts from
     * @return RealmList of Contacts
     */
    public static RealmList<RealmGroup> getRealmGroups(List<Group> groupArrayList) {
        if (groupArrayList != null) {
            RealmList<RealmGroup> realmGroupArray = new RealmList<>();
            RealmGroup realmGroup = new RealmGroup();

            for (Group group : groupArrayList) {
                realmGroup.setGroupId(group.groupId());
                realmGroup.setLabel(group.label());
                realmGroup.setChannels(getRealmChannels(group.channels()));
                realmGroup.setContacts(getRealmContacts(group.contacts()));
                realmGroupArray.add(realmGroup);
            }
            return realmGroupArray;
        }
        return null;
    }

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
