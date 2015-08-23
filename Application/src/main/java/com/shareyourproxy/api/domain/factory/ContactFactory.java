package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.realm.RealmString;

import java.util.HashMap;

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
    public static HashMap<String, Id> getModelContactList(
        RealmList<RealmString> realmContactsArray) {
        if (realmContactsArray != null) {
            HashMap<String, Id> contactList = new HashMap<>(realmContactsArray.size());
            for (RealmString realmContact : realmContactsArray) {
                contactList.put(realmContact.getValue(), Id.create(realmContact.getValue()));
            }
            return contactList;
        }
        return null;
    }

    public static HashMap<String, Id> getContactIds(RealmList<RealmString> values) {
        HashMap<String, Id> channels = new HashMap<>(values.size());
        for (RealmString value : values) {
            channels.put(value.getValue(), Id.create(value.getValue()));
        }
        return channels;
    }
}
