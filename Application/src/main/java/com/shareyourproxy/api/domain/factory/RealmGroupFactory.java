package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.realm.RealmGroup;

import java.util.HashMap;
import java.util.Map;

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
     * @param groupHashMap to get contacts from
     * @return RealmList of Contacts
     */
    public static RealmList<RealmGroup> getRealmGroups(HashMap<String, Group> groupHashMap) {
            RealmList<RealmGroup> realmGroupArray = new RealmList<>();
            for (Map.Entry<String, Group> entryGroup : groupHashMap.entrySet()) {
                Group group = entryGroup.getValue();
                RealmGroup realmGroup = new RealmGroup();
                realmGroup.setId(group.id().value());
                realmGroup.setLabel(group.label());
                realmGroup.setChannels(getRealmChannels(group.channels()));
                realmGroup.setContacts(getRealmContacts(group.contacts()));
                realmGroupArray.add(realmGroup);
            }
            return realmGroupArray;
    }
}
