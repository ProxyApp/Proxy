package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.realm.RealmString;

import java.util.HashSet;

import io.realm.RealmList;

/**
 * Factory for creating domain model {@link Contact}s.
 */
public class ContactFactory {


    /**
     * Return a RealmList of Contacts from a user.
     *
     * @param realmContactsArray to get contacts from
     * @return RealmList of Contacts
     */
    public static HashSet<String> getModelContactList(RealmList<RealmString> realmContactsArray) {
        if (realmContactsArray != null) {
            HashSet<String> contactList = new HashSet<>(realmContactsArray.size());
            for (RealmString realmContact : realmContactsArray) {
                contactList.add(realmContact.getValue());
            }
            return contactList;
        }
        return null;
    }

    public static HashSet<String> getContactIds(RealmList<RealmString> values) {
        HashSet<String> channels = new HashSet<>(values.size());
        for (RealmString value : values) {
            channels.add(value.getValue());
        }
        return channels;
    }
}
