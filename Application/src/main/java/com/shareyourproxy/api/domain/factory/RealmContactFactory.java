package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.realm.RealmContact;
import com.shareyourproxy.api.domain.realm.RealmString;

import java.util.HashSet;

import io.realm.RealmList;

/**
 * Factory for creating {@link RealmContact}s.
 */
public class RealmContactFactory {
    /**
     * Return a RealmList of Contacts from a user's contacts.
     *
     * @param contacts array of user contacts
     * @return RealmList of Contacts
     */
    public static RealmList<RealmString> getRealmContacts(HashSet<String> contacts) {
        if (contacts == null) {
            return null;
        }
        RealmList<RealmString> realmContactArray = new RealmList<>();
        for (String id : contacts) {
            RealmString realmContact = new RealmString();
            realmContact.setValue(id);
            realmContactArray.add(realmContact);
        }
        return realmContactArray;
    }
}
