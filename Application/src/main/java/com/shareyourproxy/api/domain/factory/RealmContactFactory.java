package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.realm.RealmContact;
import com.shareyourproxy.api.domain.realm.RealmString;

import java.util.HashMap;
import java.util.Map;

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
    public static RealmList<RealmString> getRealmContacts(HashMap<String, Id> contacts) {
        if (contacts == null) {
            return null;
        }
        RealmList<RealmString> realmContactArray = new RealmList<>();
        for (Map.Entry<String, Id> contactEntry : contacts.entrySet()) {
            RealmString realmContact = new RealmString();
            realmContact.setValue(contactEntry.getKey());
            realmContactArray.add(realmContact);
        }
        return realmContactArray;
    }
}
