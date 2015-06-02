package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.realm.RealmGroup;

import java.util.ArrayList;

import io.realm.RealmList;

import static com.shareyourproxy.api.domain.factory.RealmChannelFactory.getRealmChannels;
import static com.shareyourproxy.api.domain.factory.RealmContactFactory.getRealmContacts;

/**
 * Factory for creating {@link RealmGroup}s.
 */
public class RealmGroupFactory {

    /**
     * Return a RealmList of Contacts from a user
     *
     * @param groupArrayList to get contacts from
     * @return RealmList of Contacts
     */
    public static RealmList<RealmGroup> getRealmGroups(ArrayList<Group> groupArrayList) {
        if (groupArrayList != null) {
            RealmList<RealmGroup> realmGroupArray = new RealmList<>();
            RealmGroup realmGroup = new RealmGroup();

            for (Group group : groupArrayList) {
                realmGroup.setId(group.id().value());
                realmGroup.setLabel(group.label());
                realmGroup.setChannels(getRealmChannels(group.channels()));
                realmGroup.setContacts(getRealmContacts(group.contacts()));
                realmGroupArray.add(realmGroup);
            }
            return realmGroupArray;
        }
        return null;
    }
}
